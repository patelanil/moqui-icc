/*
 * This software is in the public domain under CC0 1.0 Universal plus a
 * Grant of Patent License.
 *
 * To the extent possible under law, the author(s) have dedicated all
 * copyright and related and neighboring rights to this software to the
 * public domain worldwide. This software is distributed without any
 * warranty.
 *
 * You should have received a copy of the CC0 Public Domain Dedication
 * along with this software (see the LICENSE.md file). If not, see
 * <http://creativecommons.org/publicdomain/zero/1.0/>.
 */
package co.hotwax.auth

import groovy.transform.CompileStatic
import org.apache.shiro.authc.*
import org.apache.shiro.authc.credential.CredentialsMatcher
import org.apache.shiro.authz.Authorizer
import org.apache.shiro.authz.Permission
import org.apache.shiro.authz.UnauthorizedException
import org.apache.shiro.realm.Realm
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.util.SimpleByteSource
import org.moqui.BaseArtifactException
import org.moqui.Moqui
import org.moqui.context.PasswordChangeRequiredException
import org.moqui.context.SecondFactorRequiredException
import org.moqui.entity.EntityCondition
import org.moqui.entity.EntityList
import org.moqui.entity.EntityValue
import org.moqui.impl.context.ArtifactExecutionFacadeImpl
import org.moqui.impl.context.ExecutionContextFactoryImpl
import org.moqui.impl.context.ExecutionContextImpl
import org.moqui.impl.context.UserFacadeImpl
import org.moqui.util.MNode
import org.moqui.util.WebUtilities
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.moqui.impl.util.MoquiShiroRealm.ForceLoginToken;

import java.sql.Timestamp

@CompileStatic
class OfbizShiroRealm implements Realm, Authorizer {
    protected final static Logger logger = LoggerFactory.getLogger(OfbizShiroRealm.class);

    protected ExecutionContextFactoryImpl ecfi
    protected String realmName = "moquiRealm"

    protected Class<? extends AuthenticationToken> authenticationTokenClass = UsernamePasswordToken.class

    OfbizShiroRealm() {
        // with this sort of init we may only be able to get ecfi through static reference
        this.ecfi = (ExecutionContextFactoryImpl) Moqui.executionContextFactory
    }

    OfbizShiroRealm(ExecutionContextFactoryImpl ecfi) {
        this.ecfi = ecfi
    }

    void setName(String n) { realmName = n }

    @Override
    String getName() { return realmName }

    //Class getAuthenticationTokenClass() { return authenticationTokenClass }
    //void setAuthenticationTokenClass(Class<? extends AuthenticationToken> atc) { authenticationTokenClass = atc }

    @Override
    boolean supports(AuthenticationToken token) {
        return token != null && authenticationTokenClass.isAssignableFrom(token.getClass())
    }

    static EntityValue loginPrePassword(ExecutionContextImpl eci, String username) {
        //Check the UserLogin record
        EntityValue newUserLogin = eci.entity.find("org.apache.ofbiz.security.login.UserLogin").condition("userLoginId", username)
                .useCache(false).disableAuthz().one()

        //Check the ignore case UserLogin record
        if (newUserLogin == null) {
            // case-insensitive lookup by username
            EntityCondition usernameCond = eci.entityFacade.getConditionFactory()
                    .makeCondition("userLoginId", EntityCondition.ComparisonOperator.EQUALS, username).ignoreCase()
            newUserLogin = eci.entity.find("org.apache.ofbiz.security.login.UserLogin").condition(usernameCond).disableAuthz().one()
        }

        // no account found?
        if (newUserLogin == null) throw new UnknownAccountException(eci.resource.expand('No account found for username ${username}','',[username:username]))

        boolean isSystem = "Y".equalsIgnoreCase(newUserLogin.getString("isSystem"));
        // check for disabled account before checking password (otherwise even after disable could determine if
        //    password is correct or not
        if ("N".equals(newUserLogin.getNoCheckSimple("enabled")) && !isSystem) {
            if (newUserLogin.getNoCheckSimple("disabledDateTime") != null) {
                // account temporarily disabled (probably due to excessive attempts
                Integer disabledMinutes = eci.ecfi.confXmlRoot.first("user-facade").first("login").attribute("disable-minutes") as Integer ?: 30I
                Timestamp reEnableTime = new Timestamp(newUserLogin.getTimestamp("disabledDateTime").getTime() + (disabledMinutes.intValue()*60I*1000I))

                if (reEnableTime > eci.user.nowTimestamp) {
                    // only blow up if the re-enable time is not passed
                    eci.service.sync().name("co.hotwax.common.UserServices.increment#UserLoginFailedLogins")
                            .parameter("userId", newUserLogin.userLoginId).requireNewTransaction(true).call()
                    throw new ExcessiveAttemptsException(eci.resource.expand('Authenticate failed for user ${newUserAccount.userLoginId} because account is disabled and will not be re-enabled until ${reEnableTime} [DISTMP].',
                            '', [newUserAccount:newUserLogin, reEnableTime:reEnableTime]))
                }
            } else {
                // account permanently disabled
                newUserLogin.successiveFailedLogins = newUserLogin.getLong("successiveFailedLogins") ? newUserLogin.getLong("successiveFailedLogins") + 1 : 1L;
                Long maxFailures = eci.ecfi.confXmlRoot.first('user-facade').first('login').attribute('max-failures') != null ? eci.ecfi.confXmlRoot.first('user-facade').first('login').attribute('max-failures').toLong() : 3L;
                if (newUserLogin.getLong("successiveFailedLogins") > maxFailures && newUserLogin.enabled != 'N') {
                    eci.service.sync().name("co.hotwax.common.UserServices.increment#UserLoginFailedLogins")
                            .parameters((Map<String, Object>) [userId:newUserLogin.userLoginId]).requireNewTransaction(true).call()
                }
                throw new DisabledAccountException(eci.resource.expand('Authenticate failed for user ${newUserAccount.userLoginId} because account is disabled and is not schedule to be automatically re-enabled [DISPRM].',
                        '', [newUserAccount:newUserLogin]))
            }
        }
        return newUserLogin
    }

    static void loginPostPassword(ExecutionContextImpl eci, EntityValue userLogin, AuthenticationToken token) {
        // the password did match, but check a few additional things
        String userLoginId = userLogin.getNoCheckSimple("userLoginId")
        String locale = userLogin.getNoCheckSimple("lastLocale")

        EntityValue newUserAccount = eci.entity.find("moqui.security.UserAccount").condition("externalUserId", userLoginId)
                .useCache(false).disableAuthz().one()
        if (newUserAccount == null) {
            // case-insensitive lookup by username
            EntityCondition usernameCond = eci.entityFacade.getConditionFactory()
                    .makeCondition("username", EntityCondition.ComparisonOperator.EQUALS, userLoginId).ignoreCase()
            newUserAccount = eci.entity.find("moqui.security.UserAccount").condition(usernameCond).disableAuthz().one()
        }

        EntityValue userLoginName = eci.entity.find("co.hotwax.party.UserLoginNameView").condition("userLoginId", userLoginId)
                .useCache(true).disableAuthz().one()
        if (newUserAccount) {
            String salt = newUserAccount.passwordSalt ?: null;
            String credentials = token.getCredentials();
            String userId = newUserAccount.userId
            eci.ecfi.serviceFacade.sync().name("update#moqui.security.UserAccount")
                    .parameter("userId", userId)
                    .parameter("partyId", userLoginName.partyId)
                    .parameter("externalUserId", userLoginId)
                    .parameter("username", userLoginId)
                    .parameter("userFullName", "${userLoginName.firstName?:''} ${userLoginName.lastName?:''} ${userLoginName.groupName?:''}")
                    .parameter("passwordHashType", eci.ecfi.getPasswordHashType())
                    .parameter("currentPassword", eci.ecfi.getSimpleHash(credentials, salt))
                    .parameter("hasLoggedOut", "N")
                    .parameter("disabled", "N")
                    .parameter("disabledDateTime", null)
                    .parameter("locale", locale)
                    .disableAuthz()
                    .call()
        } else {
            String salt = eci.ecfi.randomSalt;
            String credentials = token.getCredentials();
            Map createAccountOut = eci.ecfi.serviceFacade.sync().name("create#moqui.security.UserAccount")
                    .parameter("externalUserId", userLoginId)
                    .parameter("username", userLoginId)
                    .parameter("partyId", userLoginName.partyId)
                    .parameter("userFullName", "${userLoginName.firstName?:''} ${userLoginName.lastName?:''} ${userLoginName.groupName?:''}")
                    .parameter("passwordHashType", eci.ecfi.getPasswordHashType())
                    .parameter("currentPassword", eci.ecfi.getSimpleHash(credentials, salt))
                    .parameter("passwordSalt", salt)
                    .parameter("hasLoggedOut", "N")
                    .parameter("disabled", "N")
                    .parameter("disabledDateTime", null)
                    .parameter("locale", locale)
                    .disableAuthz()
                    .call()
            String userId = createAccountOut.userId
            newUserAccount = eci.entity.find("moqui.security.UserAccount").condition("userId", userId)
                    .useCache(false).disableAuthz().one()
            //TODO: This is for now handling to assign ADMIN security group while new user creation
            eci.ecfi.serviceFacade.sync().name("create#moqui.security.UserGroupMember")
                    .parameter("userGroupId", "ADMIN")
                    .parameter("userId", userId)
                    .parameter("fromDate", eci.user.nowTimestamp)
                    .disableAuthz()
                    .call()
        }

        // check for require password change
        if ("Y".equals(userLogin.getNoCheckSimple("requirePasswordChange"))) {
            // NOTE: don't call incrementUserLoginFailedLogins here (don't need compounding reasons to stop access)
            throw new PasswordChangeRequiredException(eci.resource.expand('Authenticate failed for user [${userLogin.userLoginId}] because account requires password change [PWDCHG].','',[userLogin:userLogin]))
        }
        EntityList passwordHistoryList = eci.entity.find("org.apache.ofbiz.security.login.UserLoginPasswordHistory").condition("userLoginId", userLogin.getString("userLoginId"))
                .useCache(false).conditionDate("fromDate","thruDate", eci.user.getNowTimestamp()).disableAuthz().orderBy("-fromDate").list()
        if (!passwordHistoryList.isEmpty()) {
            EntityValue passwordHistory = passwordHistoryList.get(0);

            // check time since password was last changed, if it has been too long (user-facade.password.@change-weeks default 12) then fail
            int changeWeeks = (eci.ecfi.confXmlRoot.first("user-facade").first("password").attribute("change-weeks") ?: 12) as int
            if (changeWeeks > 0) {
                int wksSinceChange = ((eci.user.nowTimestamp.time - passwordHistory.getTimestamp("fromDate").time) / (7*24*60*60*1000)).intValue()
                if (wksSinceChange > changeWeeks) {
                    // NOTE: don't call incrementUserLoginFailedLogins here (don't need compounding reasons to stop access)
                    throw new ExpiredCredentialsException(eci.resource.expand('Authenticate failed for user ${userLogin.userLoginId} because password was changed ${wksSinceChange} weeks ago and must be changed every ${changeWeeks} weeks [PWDTIM].',
                            '', [userLogin:userLogin, wksSinceChange:wksSinceChange, changeWeeks:changeWeeks]))
                }
            }
        }

        // check if the user requires an additional authentication factor step
        // do this after checking for require password change and expired password for better user experience
        if (!(token instanceof ForceLoginToken)) {
            boolean secondReqd = eci.ecfi.serviceFacade.sync().name("org.moqui.impl.UserServices.get#UserAuthcFactorRequired")
                    .parameter("userId", newUserAccount.userId).disableAuthz().call()?.secondFactorRequired ?: false
            // if the user requires authentication, throw a SecondFactorRequiredException so that UserFacadeImpl.groovy can catch the error and perform the appropriate action.
            if (secondReqd) {
                throw new SecondFactorRequiredException(eci.ecfi.resource.expand('Authentication code required for user ${username}',
                        '',[username:newUserAccount.getNoCheckSimple("username")]))
            }
        }

        // check ipAllowed if on UserAccount or any UserGroup a member of
        String clientIp = eci.userFacade.getClientIp()
        if (clientIp == null || clientIp.isEmpty()) {
            if (eci.web != null) logger.warn("Web login with no client IP for userId ${newUserAccount.userId}, not checking ipAllowed")
        } else {
            if (clientIp.contains(":")) {
                logger.warn("Web login with IPv6 client IP ${clientIp} for userId ${newUserAccount.userId}, not checking ipAllowed")
            } else {
                ArrayList<String> ipAllowedList = new ArrayList<>()
                String uaIpAllowed = newUserAccount.getNoCheckSimple("ipAllowed")
                if (uaIpAllowed != null && !uaIpAllowed.isEmpty()) ipAllowedList.add(uaIpAllowed)

                EntityList ugmList = eci.entityFacade.find("moqui.security.UserGroupMember")
                        .condition("userId", newUserAccount.getNoCheckSimple("userId"))
                        .disableAuthz().useCache(true).list()
                        .filterByDate(null, null, eci.userFacade.nowTimestamp)
                ArrayList<String> userGroupIdList = new ArrayList<>()
                for (EntityValue ugm in ugmList) userGroupIdList.add((String) ugm.get("userGroupId"))
                userGroupIdList.add("ALL_USERS")
                EntityList ugList = eci.entityFacade.find("moqui.security.UserGroup")
                        .condition("ipAllowed", EntityCondition.IS_NOT_NULL, null)
                        .condition("userGroupId", EntityCondition.IN, userGroupIdList).disableAuthz().useCache(false).list()
                for (EntityValue ug in ugList) ipAllowedList.add((String) ug.getNoCheckSimple("ipAllowed"))

                int ipAllowedListSize = ipAllowedList.size()
                if (ipAllowedListSize > 0) {
                    boolean anyMatches = false
                    for (int i = 0; i < ipAllowedListSize; i++) {
                        String pattern = (String) ipAllowedList.get(i)
                        if (WebUtilities.ip4Matches(pattern, clientIp)) {
                            anyMatches = true
                            break
                        }
                    }
                    if (!anyMatches) throw new AccountException(
                            eci.resource.expand('Authenticate failed for user ${newUserAccount.username} because client IP ${clientIp} is not in allowed list for user or group.',
                                    '', [newUserAccount:newUserAccount, clientIp:clientIp]))
                }
            }
        }

        // no more auth failures? record the various account state updates, hasLoggedOut=N
        if (userLogin.getNoCheckSimple("successiveFailedLogins") || "N".equals(userLogin.getNoCheckSimple("enabled")) ||
                userLogin.getNoCheckSimple("disabledDateTime") != null || "Y".equals(userLogin.getNoCheckSimple("hasLoggedOut"))) {
            try {
                eci.service.sync().name("update", "org.apache.ofbiz.security.login.UserLogin")
                        .parameters([userLoginId:userLogin.userLoginId, successiveFailedLogins:0, enabled:"Y", disabledDateTime:null, hasLoggedOut:"N"])
                        .disableAuthz().call()
            } catch (Exception e) {
                logger.warn("Error resetting UserAccount login status", e)
            }
        }
        // update visit if no user in visit yet
        String visitId = eci.userFacade.getVisitId()
        EntityValue visit = eci.entityFacade.find("moqui.server.Visit").condition("visitId", visitId).disableAuthz().one()
        if (visit != null) {
            if (!visit.getNoCheckSimple("userId")) {
                eci.service.sync().name("update", "moqui.server.Visit").parameter("visitId", visit.visitId)
                        .parameter("userId", newUserAccount.userId).disableAuthz().call()
            }
            if (!visit.getNoCheckSimple("clientIpCountryGeoId") && !visit.getNoCheckSimple("clientIpTimeZone")) {
                MNode ssNode = eci.ecfi.confXmlRoot.first("server-stats")
                if (ssNode.attribute("visit-ip-info-on-login") != "false") {
                    eci.service.async().name("org.moqui.impl.ServerServices.get#VisitClientIpData")
                            .parameter("visitId", visit.visitId).call()
                }
            }
        }
    }

    static void loginSaveHistory(ExecutionContextImpl eci, String userId, String passwordUsed, boolean successful) {
        // track the UserLoginHistory, whether the above succeeded or failed (ie even if an exception was thrown)
        if (!eci.getSkipStats()) {
            MNode loginNode = eci.ecfi.confXmlRoot.first("user-facade").first("login")
            if (userId != null && loginNode.attribute("history-store") != "false") {
                Timestamp fromDate = eci.getUser().getNowTimestamp()
                // look for login history in the last minute, if any found don't create UserLoginHistory
                Timestamp recentDate = new Timestamp(fromDate.getTime() - 60000)

                Map<String, Object> ulhContext = [userId:userId, fromDate:fromDate,
                                                  visitId:eci.user.visitId, successfulLogin:(successful?"Y":"N")] as Map<String, Object>
                if (!successful && loginNode.attribute("history-incorrect-password") != "false") ulhContext.passwordUsed = passwordUsed

                eci.runInWorkerThread({
                    try {
                        long recentUlh = eci.entity.find("moqui.security.UserLoginHistory").condition("userId", userId)
                                .condition("fromDate", EntityCondition.GREATER_THAN, recentDate).disableAuthz().count()
                        if (recentUlh == 0) {
                            eci.ecfi.serviceFacade.sync().name("create", "moqui.security.UserLoginHistory")
                                    .parameters(ulhContext).disableAuthz().call()
                        } else {
                            if (logger.isDebugEnabled()) logger.debug("Not creating UserLoginHistory, found existing record for userId ${userId} and more recent than ${recentDate}")
                        }
                    } catch (Exception ee) {
                        // this blows up sometimes on MySQL, may in other cases, and is only so important so log a warning but don't rethrow
                        logger.warn("UserLoginHistory create failed: ${ee.toString()}")
                    }
                })
            }
        }
    }

    @Override
    AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        ExecutionContextImpl eci = ecfi.getEci()
        String username = token.principal as String
        String userId = null
        boolean successful = false
        boolean isForceLogin = token instanceof ForceLoginToken

        SaltedAuthenticationInfo info = null
        try {
            EntityValue newUserAccount = loginPrePassword(eci, username)
            userId = newUserAccount.getString("userLoginId")

            // create the salted SimpleAuthenticationInfo object
            info = new SimpleAuthenticationInfo(username, newUserAccount.currentPassword,null, realmName)
            if (!isForceLogin) {
                // check the password (credentials for this case)
                CredentialsMatcher cm =  new PosixCredentialsMatcher();
                if (!cm.doCredentialsMatch(token, info)) {
                    // if failed on password, increment in new transaction to make sure it sticks
                    ecfi.serviceFacade.sync().name("co.hotwax.common.UserServices.increment#UserLoginFailedLogins")
                            .parameters((Map<String, Object>) [userId:newUserAccount.userLoginId]).requireNewTransaction(true).call()
                    throw new IncorrectCredentialsException(ecfi.resource.expand('Password incorrect for username ${username}','',[username:username]))
                }
            }

            // credentials matched
            loginPostPassword(eci, newUserAccount, token)

            // at this point the user is successfully authenticated
            successful = true
        } finally {
            boolean saveHistory = true
            if (isForceLogin) {
                ForceLoginToken flt = (ForceLoginToken) token
                saveHistory = flt.saveHistory
            }
            if (saveHistory) loginSaveHistory(eci, userId, token.credentials as String, successful)
        }

        return info
    }

    static boolean checkCredentials(String username, String password, ExecutionContextFactoryImpl ecfi) {
        EntityValue newUserAccount = ecfi.entity.find("moqui.security.UserAccount").condition("username", username)
                .useCache(true).disableAuthz().one()

        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(username, newUserAccount.currentPassword,
                newUserAccount.passwordSalt ? new SimpleByteSource((String) newUserAccount.passwordSalt) : null, "moquiRealm")

        CredentialsMatcher cm = new PosixCredentialsMatcher();
        //CredentialsMatcher cm = ecfi.getCredentialsMatcher((String) newUserAccount.passwordHashType, "Y".equals(newUserAccount.passwordBase64))
        UsernamePasswordToken token = new UsernamePasswordToken(username, password)
        return cm.doCredentialsMatch(token, info)
    }

    // ========== Authorization Methods ==========

    /**
     * @param principalCollection The principal (user)
     * @param resourceAccess Formatted as: "${typeEnumId}:${actionEnumId}:${name}"
     * @return boolean true if principal is permitted to access the resource, false otherwise.
     */
    boolean isPermitted(PrincipalCollection principalCollection, String resourceAccess) {
        // String username = (String) principalCollection.primaryPrincipal
        // TODO: if we want to support other users than the current need to look them up here
        return ArtifactExecutionFacadeImpl.isPermitted(resourceAccess, ecfi.getEci())
    }

    boolean[] isPermitted(PrincipalCollection principalCollection, String... resourceAccesses) {
        boolean[] resultArray = new boolean[resourceAccesses.size()]
        int i = 0
        for (String resourceAccess in resourceAccesses) {
            resultArray[i] = this.isPermitted(principalCollection, resourceAccess)
            i++
        }
        return resultArray
    }

    boolean isPermittedAll(PrincipalCollection principalCollection, String... resourceAccesses) {
        for (String resourceAccess in resourceAccesses)
            if (!this.isPermitted(principalCollection, resourceAccess)) return false
        return true
    }

    boolean isPermitted(PrincipalCollection principalCollection, Permission permission) {
        throw new BaseArtifactException("Authorization of Permission through Shiro not yet supported")
    }

    boolean[] isPermitted(PrincipalCollection principalCollection, List<Permission> permissions) {
        throw new BaseArtifactException("Authorization of Permission through Shiro not yet supported")
    }

    boolean isPermittedAll(PrincipalCollection principalCollection, Collection<Permission> permissions) {
        throw new BaseArtifactException("Authorization of Permission through Shiro not yet supported")
    }

    void checkPermission(PrincipalCollection principalCollection, Permission permission) {
        // TODO how to handle the permission interface?
        // see: http://www.jarvana.com/jarvana/view/org/apache/shiro/shiro-core/1.1.0/shiro-core-1.1.0-javadoc.jar!/org/apache/shiro/authz/Permission.html
        // also look at DomainPermission, can extend for Moqui artifacts
        // this.checkPermission(principalCollection, permission.?)
        throw new BaseArtifactException("Authorization of Permission through Shiro not yet supported")
    }

    void checkPermission(PrincipalCollection principalCollection, String permission) {
        String username = (String) principalCollection.primaryPrincipal
        if (UserFacadeImpl.hasPermission(username, permission, null, ecfi.getEci())) {
            throw new UnauthorizedException(ecfi.resource.expand('User ${username} does not have permission ${permission}','',[username:username,permission:permission]))
        }
    }

    void checkPermissions(PrincipalCollection principalCollection, String... strings) {
        for (String permission in strings) checkPermission(principalCollection, permission)
    }

    void checkPermissions(PrincipalCollection principalCollection, Collection<Permission> permissions) {
        for (Permission permission in permissions) checkPermission(principalCollection, permission)
    }

    boolean hasRole(PrincipalCollection principalCollection, String roleName) {
        String username = (String) principalCollection.primaryPrincipal
        return UserFacadeImpl.isInGroup(username, roleName, null, ecfi.getEci())
    }

    boolean[] hasRoles(PrincipalCollection principalCollection, List<String> roleNames) {
        boolean[] resultArray = new boolean[roleNames.size()]
        int i = 0
        for (String roleName in roleNames) { resultArray[i] = this.hasRole(principalCollection, roleName); i++ }
        return resultArray
    }

    boolean hasAllRoles(PrincipalCollection principalCollection, Collection<String> roleNames) {
        for (String roleName in roleNames) { if (!this.hasRole(principalCollection, roleName)) return false }
        return true
    }

    void checkRole(PrincipalCollection principalCollection, String roleName) {
        if (!this.hasRole(principalCollection, roleName))
            throw new UnauthorizedException(ecfi.resource.expand('User ${principalCollection.primaryPrincipal} is not in role ${roleName}','',[principalCollection:principalCollection,roleName:roleName]))
    }

    void checkRoles(PrincipalCollection principalCollection, Collection<String> roleNames) {
        for (String roleName in roleNames) {
            if (!this.hasRole(principalCollection, roleName))
                throw new UnauthorizedException(ecfi.resource.expand('User ${principalCollection.primaryPrincipal} is not in role ${roleName}','',[principalCollection:principalCollection,roleName:roleName]))
        }
    }

    void checkRoles(PrincipalCollection principalCollection, String... roleNames) {
        for (String roleName in roleNames) {
            if (!this.hasRole(principalCollection, roleName))
                throw new UnauthorizedException(ecfi.resource.expand('User ${principalCollection.primaryPrincipal} is not in role ${roleName}','',[principalCollection:principalCollection,roleName:roleName]))
        }
    }
}
