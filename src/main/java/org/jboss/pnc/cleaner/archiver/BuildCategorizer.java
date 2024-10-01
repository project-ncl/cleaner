/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2019-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.cleaner.archiver;

import lombok.Value;

import static org.jboss.pnc.cleaner.archiver.ArchivedBuildRecord.ErrorGroup.INDY;
import static org.jboss.pnc.cleaner.archiver.ArchivedBuildRecord.ErrorGroup.ND;
import static org.jboss.pnc.cleaner.archiver.ArchivedBuildRecord.ErrorGroup.PNC;
import static org.jboss.pnc.cleaner.archiver.ArchivedBuildRecord.ErrorGroup.PSI;

public class BuildCategorizer {
    public static final String EXCEPTION_TRYING_TO_GET_HTTPS_PAAS_HEALTHZ_READY = "Exception trying to GET https://paas.*/healthz/ready";
    public static final String EXCEPTION_TRYING_TO_GET_HTTPS_PAAS_APIS_SERVICECATALOG_K_8_S_IO_V_1_BETA_1 = "Exception trying to GET https://paas.*/apis/servicecatalog.k8s.io/v1beta1";
    public static final String UNABLE_TO_READ_ENDPOINT_HTTPS_PAAS_APIS_CERTIFICATES_K_8_S_IO_V_1_BETA_1 = "Unable to read endpoint https://paas.*//apis/certificates.k8s.io/v1beta1";
    public static final String EXCEPTION_TRYING_TO_POST_HTTPS_PAAS_API_V_1_NAMESPACES_NEWCASTLE_BUILDERS = "Exception trying to POST https://paas.*/v1/namespaces/newcastle-builders";
    public static final String UNABLE_TO_EXECUTE_REQUEST_TO_HTTPS_PAAS_API_V_1_NAMESPACES_NEWCASTLE_BUILDERS = "Unable to execute request to https://paas.*/v1/namespaces/newcastle-builders";
    public static final String REQUEST_TO_ENDPOINT_HTTP_REPOUR_ADJUST_FAILED_HTTP_1_0_503 = "Request to endpoint http://repour.*/adjust failed: HTTP/1.0 503";
    public static final String REQUEST_TO_ENDPOINT_HTTP_REPOUR_ADJUST_FAILED_HTTP_1_0_504 = "Request to endpoint http://repour.*/adjust failed: HTTP/1.0 504";
    public static final String REST_COMMUNICATION_WITH_HTTP_DA_DA_REST_V_1_FAILED = "REST communication with http://da.*/da/rest/v-1 failed.";
    public static final String JAVA_NET_UNKNOWN_HOST_EXCEPTION_ORCH = "java.net.UnknownHostException: orch.*";
    public static final String DA_80_FAILED_TO_RESPOND = "da.*:80 failed to respond";
    public static final String REQUEST_TO_ENDPOINT_HTTP_REPOUR_ADJUST_FAILED_HTTP_1_1_500 = "Request to endpoint http://repour.*/adjust failed: HTTP/1.1 500";
    public static final String BPM_NEW_BASE_URL_HTTPS_DEVKIESERVER_NEWCASTLE_DEVEL_SERVICES_REST_SERVER_CONTAINERS = "bpmNewBaseUrl=https://devkieserver-newcastle-devel.*/services/rest/server/containers/";
    public static final String INDY_80_FAILED_TO_RESPOND = "indy.*:80 failed to respond";
    public static final String INDY_GATEWAY_443_FAILED_TO_RESPOND = "indy-gateway.*:443 failed to respond";
    public static final String USER_CANNOT_CREATE = "User \".*\" cannot create";
    public static final String[] regExpErrors = {
            EXCEPTION_TRYING_TO_GET_HTTPS_PAAS_HEALTHZ_READY,
            EXCEPTION_TRYING_TO_GET_HTTPS_PAAS_APIS_SERVICECATALOG_K_8_S_IO_V_1_BETA_1,
            UNABLE_TO_READ_ENDPOINT_HTTPS_PAAS_APIS_CERTIFICATES_K_8_S_IO_V_1_BETA_1,
            EXCEPTION_TRYING_TO_POST_HTTPS_PAAS_API_V_1_NAMESPACES_NEWCASTLE_BUILDERS,
            UNABLE_TO_EXECUTE_REQUEST_TO_HTTPS_PAAS_API_V_1_NAMESPACES_NEWCASTLE_BUILDERS,
            REQUEST_TO_ENDPOINT_HTTP_REPOUR_ADJUST_FAILED_HTTP_1_0_503,
            REQUEST_TO_ENDPOINT_HTTP_REPOUR_ADJUST_FAILED_HTTP_1_0_504,
            REST_COMMUNICATION_WITH_HTTP_DA_DA_REST_V_1_FAILED,
            JAVA_NET_UNKNOWN_HOST_EXCEPTION_ORCH,
            DA_80_FAILED_TO_RESPOND,
            REQUEST_TO_ENDPOINT_HTTP_REPOUR_ADJUST_FAILED_HTTP_1_1_500,
            BPM_NEW_BASE_URL_HTTPS_DEVKIESERVER_NEWCASTLE_DEVEL_SERVICES_REST_SERVER_CONTAINERS,
            INDY_80_FAILED_TO_RESPOND,
            INDY_GATEWAY_443_FAILED_TO_RESPOND,
            USER_CANNOT_CREATE, };
    public static final String UNAUTHORIZED_TO_ACCESS_RESOURCE = "Unauthorized to access resource";
    public static final String EXCEEDED_QUOTA_NEWCASTLE_BUILDERS_QUOTA = "exceeded quota: newcastle-builders-quota";
    public static final String EXCEPTION_WHILE_TRYING_TO_DETERMINE_THE_HEALTH_READY_RESPONSE_OF_THE_SERVER = "Exception while trying to determine the health/ready response of the server";
    public static final String FAILED_TO_ALLOCATE_A_SERVICE_IP_ETCDSERVER = "failed to allocate a serviceIP: etcdserver";
    public static final String POD_FAILED_WITH_STATUS_ERR_IMAGE_PULL = "Pod failed with status: ErrImagePull";
    public static final String SERVICE_WAS_NOT_READY_IN_300_SECONDS = "Service was not ready in: 300 SECONDS";
    public static final String OPERATION_CANNOT_BE_FULFILLED_ON_RESOURCEQUOTAS_NEWCASTLE_BUILDERS_QUOTA = "Operation cannot be fulfilled on resourcequotas \"newcastle-builders-quota\"";
    public static final String TIMEOUT_EXCEPTION_CONDITION_WAS_NOT_SATISFIED_IN_300_SECONDS = "TimeoutException: Condition was not satisfied in: 300 SECONDS";
    public static final String TIMEOUT_EXCEPTION_CONDITION_WAS_NOT_SATISFIED_IN_600_SECONDS = "TimeoutException: Condition was not satisfied in: 600 SECONDS";
    public static final String COM_OPENSHIFT_RESTCLIENT_NOT_FOUND_EXCEPTION_NOT_FOUND = "com.openshift.restclient.NotFoundException: Not Found";
    public static final String FAILED_TO_CONNECT_TO_REMOTE_CLIENT = "Failed to connect to remote client";
    public static final String NO_ROUTE_TO_HOST = "No route to host";
    public static final String COULD_NOT_UPLOAD_BUILD_SCRIPT = "Could not upload build script";
    public static final String NO_ROUTE_TO_HOST_HOST_UNREACHABLE = "No route to host (Host unreachable)";
    public static final String BROKEN_PIPE_WRITE_FAILED = "Broken pipe (Write failed)";
    static final String[] psiErrors = {
            UNAUTHORIZED_TO_ACCESS_RESOURCE,
            EXCEEDED_QUOTA_NEWCASTLE_BUILDERS_QUOTA,
            EXCEPTION_TRYING_TO_GET_HTTPS_PAAS_HEALTHZ_READY,
            EXCEPTION_WHILE_TRYING_TO_DETERMINE_THE_HEALTH_READY_RESPONSE_OF_THE_SERVER,
            EXCEPTION_TRYING_TO_GET_HTTPS_PAAS_APIS_SERVICECATALOG_K_8_S_IO_V_1_BETA_1,
            UNABLE_TO_READ_ENDPOINT_HTTPS_PAAS_APIS_CERTIFICATES_K_8_S_IO_V_1_BETA_1,
            EXCEPTION_TRYING_TO_POST_HTTPS_PAAS_API_V_1_NAMESPACES_NEWCASTLE_BUILDERS,
            FAILED_TO_ALLOCATE_A_SERVICE_IP_ETCDSERVER,
            REQUEST_TO_ENDPOINT_HTTP_REPOUR_ADJUST_FAILED_HTTP_1_0_503,
            REQUEST_TO_ENDPOINT_HTTP_REPOUR_ADJUST_FAILED_HTTP_1_0_504,
            POD_FAILED_WITH_STATUS_ERR_IMAGE_PULL,
            SERVICE_WAS_NOT_READY_IN_300_SECONDS,
            TIMEOUT_EXCEPTION_CONDITION_WAS_NOT_SATISFIED_IN_300_SECONDS,
            TIMEOUT_EXCEPTION_CONDITION_WAS_NOT_SATISFIED_IN_600_SECONDS,
            UNABLE_TO_EXECUTE_REQUEST_TO_HTTPS_PAAS_API_V_1_NAMESPACES_NEWCASTLE_BUILDERS,
            USER_CANNOT_CREATE,
            COM_OPENSHIFT_RESTCLIENT_NOT_FOUND_EXCEPTION_NOT_FOUND,
            JAVA_NET_UNKNOWN_HOST_EXCEPTION_ORCH,
            BROKEN_PIPE_WRITE_FAILED, };
    public static final String REPOUR_COMPLETED_WITH_SYSTEM_ERROR = "Repour completed with system error";
    public static final String FAILED_TO_SETUP_REPOSITORY_OR_REPOSITORY_GROUP_FOR_THIS_BUILD = "Failed to setup repository or repository group for this build";
    public static final String NO_ROUTE_TO_HOST_HOST_UNREACHABLE1 = "Indy request failed: No route to host (Host unreachable)";
    public static final String ERROR_WHILE_TRYING_TO_START_BUILDING_WITH_BPM_BUILD_SCHEDULER = "Error while trying to startBuilding with BpmBuildScheduler.";
    public static final String BUILD_AGENT_HAS_GONE_AWAY = "Build Agent has gone away";
    public static final String FAILED_TO_OBTAIN_VERSIONS = "Failed to obtain versions";
    public static final String RECEIVED_RESPONSE_STATUS_500 = "Received response status 500";
    public static final String READ_TIMED_OUT = "Read timed out";
    public static final String COULD_NOT_FIND_THE_GROUP_ID_IN_THE_POM_XML = "Could not find the groupId in the pom.xml";
    public static final String DMANIPULATION_DISABLE_TRUE = "-Dmanipulation.disable=true";
    public static final String DMANIPULATION_DISABLE_TRUE1 = "-DmanipulationDisable=true";
    public static final String NO_SUCH_FILE_OR_DIRECTORY = "No such file or directory";
    public static final String COULD_NOT_CONVERT_OBJECT_TO_JSON = "Could not convert object to JSON";
    public static final String INDY_REQUEST_FAILED_CONNECT_TO_INDY = "Indy request failed: Connect to indy";
    public static final String STATUS_500_INTERNAL_SERVER_ERROR = "Status: 500 Internal Server Error";
    public static final String ERROR_POSTING_WITH_PATHS_PROMOTE_RESULT_RESULT_FROM_PROMOTION_PATHS_PROMOTE = "Error POSTING with PathsPromoteResult result from: promotion/paths/promote";
    public static final String ERROR_CHECKING_EXISTENCE_OF = "Error checking existence of";
    public static final String REPOSITORY_MANAGER_EXCEPTION_FAILED_TO_PROMOTE = "RepositoryManagerException: Failed to promote";
    public static final String FAILED_TO_RESPOND = "failed to respond";
    public static final String ADD_A_PROVIDER_LIKE_HIBERNATE_VALIDATOR = "Add a provider like Hibernate Validator";
    public static final String COULD_NOT_INITIALIZE_CLASS_ORG_JBOSS_PNC_BUILDAGENT_CLIENT_BUILD_AGENT_SOCKET_CLIENT = "Could not initialize class org.jboss.pnc.buildagent.client.BuildAgentSocketClient";
    public static final String COULD_NOT_FIND_AN_IMPLEMENTATION_CLASS = "Could not find an implementation class";
    public static final String TRYING_TO_STORE_SUCCESS_BUILD_WITH_INVALID_REPOSITORY_MANAGER_RESULT_CONFLICTING_ARTIFACT = "Trying to store success build with invalid repository manager result. Conflicting artifact";
    public static final String COMPLETION_EXCEPTION_JAVA_UTIL_CONCURRENT_CANCELLATION_EXCEPTION = "CompletionException: java.util.concurrent.CancellationException";
    public static final String THROWABLE_POD_FAILED_WITH_STATUS_INVALID_IMAGE_NAME = "Throwable: Pod failed with status: InvalidImageName";
    public static final String THE_BUILDER_POD_FAILED_TO_START_THIS_COULD_BE_DUE_TO_MISCONFIGURED_OR_BOGUS_SCRIPTS_OR_OTHER_UNKNOWN_REASONS = "The builder pod failed to start (this could be due to misconfigured or bogus scripts, or other unknown reasons)";
    public static final String CAUSED_BY_JAVA_NET_CONNECT_EXCEPTION_CONNECTION_REFUSED = "Caused by: java.net.ConnectException: Connection refused";
    public static final String JAVA_NET_SOCKET_EXCEPTION_UNEXPECTED_END_OF_FILE_FROM_SERVER = "java.net.SocketException: Unexpected end of file from server";
    public static final String UNABLE_TO_RETRIEVE_CONTENT_FROM_RESPONSE = "Unable to retrieve content from response";
    public static final String NO_DEPLOYMENTS_AVAILABLE_FOR_COM_REDHAT_MAITAI_NCL_NCL_WORKFLOWS = "No deployments available for com.redhat.maitai.ncl:ncl-workflows:";
    public static final String JAVA_NET_SOCKET_TIMEOUT_EXCEPTION_READ_TIMED_OUT = "java.net.SocketTimeoutException: Read timed out";
    public static final String CAUSED_BY_ORG_HIBERNATE_HIBERNATE_EXCEPTION = "Caused by: org.hibernate.HibernateException";
    public static final String ORG_HIBERNATE_EXCEPTION_GENERIC_JDBCEXCEPTION = "org.hibernate.exception.GenericJDBCException";
    public static final String CORE_EXCEPTION_ERROR_WHILE_TRYING_TO_START_BUILDING_WITH_BPM_BUILD_SCHEDULER = "CoreException: Error while trying to startBuilding with BpmBuildScheduler";
    public static final String CONNECT_TO_INDY = "Connect to indy";
    public static final String COULD_NOT_GET = "Could not GET";
    public static final String HTTP_INDY = "http://indy";
    public static final String HTTPS_INDY = "https://indy";
    public static final String READ_TIMED_OUT_ = "Read timed out ";
    public static final String FRONTEND_MAVEN_PLUGIN = "--- frontend-maven-plugin";
    public static final String EXECUTION_ROOT_NAME_PARAMETER_HAS_AS_VALUE_THE_WRONG_FORMAT = "EXECUTION_ROOT_NAME parameter has as value the wrong format";
    public static final String FILE = "--file=";
    public static final String FAILED_CONNECTION_REFUSED = "failed: Connection refused";
    public static final String FAILED_CONNECT_TIMED_OUT = "failed: connect timed out";
    public static final String DBREW_PULL_ACTIVE_TRUE = "-DbrewPullActive=true";
    static final String[] literalErrors = {
            UNAUTHORIZED_TO_ACCESS_RESOURCE,
            EXCEEDED_QUOTA_NEWCASTLE_BUILDERS_QUOTA,
            EXCEPTION_WHILE_TRYING_TO_DETERMINE_THE_HEALTH_READY_RESPONSE_OF_THE_SERVER,
            FAILED_TO_ALLOCATE_A_SERVICE_IP_ETCDSERVER,
            POD_FAILED_WITH_STATUS_ERR_IMAGE_PULL,
            SERVICE_WAS_NOT_READY_IN_300_SECONDS,
            OPERATION_CANNOT_BE_FULFILLED_ON_RESOURCEQUOTAS_NEWCASTLE_BUILDERS_QUOTA,
            TIMEOUT_EXCEPTION_CONDITION_WAS_NOT_SATISFIED_IN_300_SECONDS,
            TIMEOUT_EXCEPTION_CONDITION_WAS_NOT_SATISFIED_IN_600_SECONDS,
            COM_OPENSHIFT_RESTCLIENT_NOT_FOUND_EXCEPTION_NOT_FOUND,
            FAILED_TO_CONNECT_TO_REMOTE_CLIENT,
            NO_ROUTE_TO_HOST,
            COULD_NOT_UPLOAD_BUILD_SCRIPT,
            NO_ROUTE_TO_HOST_HOST_UNREACHABLE,
            BROKEN_PIPE_WRITE_FAILED,
            REPOUR_COMPLETED_WITH_SYSTEM_ERROR,
            FAILED_TO_SETUP_REPOSITORY_OR_REPOSITORY_GROUP_FOR_THIS_BUILD,
            NO_ROUTE_TO_HOST_HOST_UNREACHABLE1,
            ERROR_WHILE_TRYING_TO_START_BUILDING_WITH_BPM_BUILD_SCHEDULER,
            BUILD_AGENT_HAS_GONE_AWAY,
            FAILED_TO_OBTAIN_VERSIONS,
            RECEIVED_RESPONSE_STATUS_500,
            READ_TIMED_OUT,
            COULD_NOT_FIND_THE_GROUP_ID_IN_THE_POM_XML,
            DMANIPULATION_DISABLE_TRUE,
            DMANIPULATION_DISABLE_TRUE1,
            NO_SUCH_FILE_OR_DIRECTORY,
            COULD_NOT_CONVERT_OBJECT_TO_JSON,
            INDY_REQUEST_FAILED_CONNECT_TO_INDY,
            STATUS_500_INTERNAL_SERVER_ERROR,
            ERROR_POSTING_WITH_PATHS_PROMOTE_RESULT_RESULT_FROM_PROMOTION_PATHS_PROMOTE,
            ERROR_CHECKING_EXISTENCE_OF,
            REPOSITORY_MANAGER_EXCEPTION_FAILED_TO_PROMOTE,
            FAILED_TO_RESPOND,
            ADD_A_PROVIDER_LIKE_HIBERNATE_VALIDATOR,
            COULD_NOT_INITIALIZE_CLASS_ORG_JBOSS_PNC_BUILDAGENT_CLIENT_BUILD_AGENT_SOCKET_CLIENT,
            COULD_NOT_FIND_AN_IMPLEMENTATION_CLASS,
            TRYING_TO_STORE_SUCCESS_BUILD_WITH_INVALID_REPOSITORY_MANAGER_RESULT_CONFLICTING_ARTIFACT,
            COMPLETION_EXCEPTION_JAVA_UTIL_CONCURRENT_CANCELLATION_EXCEPTION,
            THROWABLE_POD_FAILED_WITH_STATUS_INVALID_IMAGE_NAME,
            THE_BUILDER_POD_FAILED_TO_START_THIS_COULD_BE_DUE_TO_MISCONFIGURED_OR_BOGUS_SCRIPTS_OR_OTHER_UNKNOWN_REASONS,
            CAUSED_BY_JAVA_NET_CONNECT_EXCEPTION_CONNECTION_REFUSED,
            JAVA_NET_SOCKET_EXCEPTION_UNEXPECTED_END_OF_FILE_FROM_SERVER,
            UNABLE_TO_RETRIEVE_CONTENT_FROM_RESPONSE,
            NO_DEPLOYMENTS_AVAILABLE_FOR_COM_REDHAT_MAITAI_NCL_NCL_WORKFLOWS,
            JAVA_NET_SOCKET_TIMEOUT_EXCEPTION_READ_TIMED_OUT,
            CAUSED_BY_ORG_HIBERNATE_HIBERNATE_EXCEPTION,
            ORG_HIBERNATE_EXCEPTION_GENERIC_JDBCEXCEPTION,
            CORE_EXCEPTION_ERROR_WHILE_TRYING_TO_START_BUILDING_WITH_BPM_BUILD_SCHEDULER,
            CONNECT_TO_INDY,
            COULD_NOT_GET,
            HTTP_INDY,
            HTTPS_INDY,
            READ_TIMED_OUT_,
            FRONTEND_MAVEN_PLUGIN,
            EXECUTION_ROOT_NAME_PARAMETER_HAS_AS_VALUE_THE_WRONG_FORMAT,
            FILE,
            FAILED_CONNECTION_REFUSED,
            FAILED_CONNECT_TIMED_OUT,
            DBREW_PULL_ACTIVE_TRUE, };

    public static LogParser getLogParser(long trimLogSize) {
        LogParser logParser = new LogParser(trimLogSize);
        logParser.addLiteralLines(literalErrors);
        logParser.addRegExpLines(regExpErrors);
        return logParser;
    }

    public static DetectedCategory categorizeErrors(LogParser buildLog, LogParser alignmentLog) {
        for (String message : psiErrors)
            if (buildLog.contains(message))
                return new DetectedCategory(PSI, buildLog.get(message));

        if (buildLog.contains(OPERATION_CANNOT_BE_FULFILLED_ON_RESOURCEQUOTAS_NEWCASTLE_BUILDERS_QUOTA)) {
            return new DetectedCategory(PSI, EXCEEDED_QUOTA_NEWCASTLE_BUILDERS_QUOTA);
        } else if (buildLog.contains(FAILED_TO_CONNECT_TO_REMOTE_CLIENT) && buildLog.contains(NO_ROUTE_TO_HOST)) {
            return new DetectedCategory(PSI, "Failed to connect to remote client. No route to host. [NCLSUP-162]");
        } else if (buildLog.contains(COULD_NOT_UPLOAD_BUILD_SCRIPT)
                && buildLog.contains(NO_ROUTE_TO_HOST_HOST_UNREACHABLE)) {
            return new DetectedCategory(
                    PSI,
                    "Could not upload build script - No route to host (Host unreachable) [NCLSUP-217]");
        } else if (buildLog.contains(REPOUR_COMPLETED_WITH_SYSTEM_ERROR)
                && alignmentLog.contains(REST_COMMUNICATION_WITH_HTTP_DA_DA_REST_V_1_FAILED)
                && alignmentLog.contains(NO_ROUTE_TO_HOST_HOST_UNREACHABLE)) {
            return new DetectedCategory(PSI, "DA - No route to host (Host unreachable)");
        } else if (buildLog.contains(FAILED_TO_SETUP_REPOSITORY_OR_REPOSITORY_GROUP_FOR_THIS_BUILD)
                && buildLog.contains(NO_ROUTE_TO_HOST_HOST_UNREACHABLE1)) {
            return new DetectedCategory(PSI, "INDY - No route to host (Host unreachable)");
        } else if (buildLog.contains(ERROR_WHILE_TRYING_TO_START_BUILDING_WITH_BPM_BUILD_SCHEDULER)
                && buildLog.contains(NO_ROUTE_TO_HOST_HOST_UNREACHABLE)) {
            return new DetectedCategory(PSI, "MAITAI - No route to host (Host unreachable)");
        } else if (buildLog.contains(BUILD_AGENT_HAS_GONE_AWAY)) {
            return new DetectedCategory(PSI, "Build Agent has gone away (Network issues)");
        }

        if (buildLog.contains(REPOUR_COMPLETED_WITH_SYSTEM_ERROR)) {
            if (alignmentLog.contains(FAILED_TO_OBTAIN_VERSIONS)) {
                return new DetectedCategory(INDY, "INDY - Failed to obtain versions");
            } else if (alignmentLog.contains(REST_COMMUNICATION_WITH_HTTP_DA_DA_REST_V_1_FAILED)) {
                if (alignmentLog.contains(RECEIVED_RESPONSE_STATUS_500)) {
                    return new DetectedCategory(PNC, "DA - Response status 500");
                } else if (alignmentLog.contains(READ_TIMED_OUT)) {
                    return new DetectedCategory(PNC, "DA - Read timed out");
                } else {
                    return new DetectedCategory(PNC, "DA - REST communication failed");
                }
            } else if (alignmentLog.contains(DA_80_FAILED_TO_RESPOND)) {
                return new DetectedCategory(PNC, "DA - Failed to respond (da.newcastle.svc.cluster.local:80)");
            } else if (alignmentLog.contains(COULD_NOT_FIND_THE_GROUP_ID_IN_THE_POM_XML)
                    && (alignmentLog.contains(DMANIPULATION_DISABLE_TRUE)
                            || alignmentLog.contains(DMANIPULATION_DISABLE_TRUE1))) {
                return new DetectedCategory(PNC, "user did not specify BREW_BUILD_VERSION or BREW_BUILD_NAME", true);
            } else if (alignmentLog.contains(EXECUTION_ROOT_NAME_PARAMETER_HAS_AS_VALUE_THE_WRONG_FORMAT)
                    && (alignmentLog.contains(DMANIPULATION_DISABLE_TRUE)
                            || alignmentLog.contains(DMANIPULATION_DISABLE_TRUE1))) {
                return new DetectedCategory(PNC, "user wrongly specified BREW_BUILD_VERSION or BREW_BUILD_NAME", true);
            } else if (alignmentLog.contains(NO_SUCH_FILE_OR_DIRECTORY) && alignmentLog.contains(FILE)) {
                return new DetectedCategory(PNC, "user wrongly specified custom pom.xml location", true);
            } else if (alignmentLog.isEmpty()) {
                return new DetectedCategory(
                        PNC,
                        "Build abortion in alignment phase causes system error [NCLSUP-248]",
                        true);
            } else {
                return new DetectedCategory(PNC, "REPOUR - completed with system error");
            }
        } else if (buildLog.contains(COULD_NOT_CONVERT_OBJECT_TO_JSON)) {
            return new DetectedCategory(PNC, COULD_NOT_CONVERT_OBJECT_TO_JSON);
        } else if (buildLog.contains(FAILED_TO_SETUP_REPOSITORY_OR_REPOSITORY_GROUP_FOR_THIS_BUILD)) {
            if (buildLog.contains(INDY_REQUEST_FAILED_CONNECT_TO_INDY)) {
                return new DetectedCategory(INDY, "INDY - Failed to respond during repository setup");
            } else if (buildLog.contains(STATUS_500_INTERNAL_SERVER_ERROR)) {
                return new DetectedCategory(INDY, "INDY - Response status 500");
            } else {
                return new DetectedCategory(PNC, FAILED_TO_SETUP_REPOSITORY_OR_REPOSITORY_GROUP_FOR_THIS_BUILD);
            }
        } else if (buildLog.contains(ERROR_POSTING_WITH_PATHS_PROMOTE_RESULT_RESULT_FROM_PROMOTION_PATHS_PROMOTE)) {
            return new DetectedCategory(INDY, "INDY - Failed to promote");
        } else if (buildLog.contains(FAILED_TO_CONNECT_TO_REMOTE_CLIENT)) {
            return new DetectedCategory(PNC, "Failed to connect to remote client [NCLSUP-53]");
        } else if (buildLog.contains(REQUEST_TO_ENDPOINT_HTTP_REPOUR_ADJUST_FAILED_HTTP_1_1_500)) {
            return new DetectedCategory(PNC, "REPOUR - Response status 500");
        } else if (buildLog.contains(ERROR_CHECKING_EXISTENCE_OF)) {
            return new DetectedCategory(PNC, "Error checking existence of [NCLSUP-51]");
        } else if (buildLog.contains(REPOSITORY_MANAGER_EXCEPTION_FAILED_TO_PROMOTE)) {
            if (buildLog.contains(FAILED_TO_RESPOND)) {
                return new DetectedCategory(INDY, "INDY - Failed to respond during promotion");
            } else {
                return new DetectedCategory(PNC, REPOSITORY_MANAGER_EXCEPTION_FAILED_TO_PROMOTE);
            }
        } else if (buildLog.contains(ADD_A_PROVIDER_LIKE_HIBERNATE_VALIDATOR)) {
            return new DetectedCategory(PNC, "Add a provider like Hibernate Validator [NCLSUP-79]");
        } else if (buildLog
                .contains(COULD_NOT_INITIALIZE_CLASS_ORG_JBOSS_PNC_BUILDAGENT_CLIENT_BUILD_AGENT_SOCKET_CLIENT)) {
            return new DetectedCategory(
                    PNC,
                    COULD_NOT_INITIALIZE_CLASS_ORG_JBOSS_PNC_BUILDAGENT_CLIENT_BUILD_AGENT_SOCKET_CLIENT);
        } else if (buildLog.contains(COULD_NOT_FIND_AN_IMPLEMENTATION_CLASS)) {
            return new DetectedCategory(PNC, COULD_NOT_FIND_AN_IMPLEMENTATION_CLASS);
        } else if (buildLog
                .contains(TRYING_TO_STORE_SUCCESS_BUILD_WITH_INVALID_REPOSITORY_MANAGER_RESULT_CONFLICTING_ARTIFACT)) {
            return new DetectedCategory(PNC, "Conflicting artifact");
        } else if (buildLog.contains(COMPLETION_EXCEPTION_JAVA_UTIL_CONCURRENT_CANCELLATION_EXCEPTION)) {
            return new DetectedCategory(PNC, COMPLETION_EXCEPTION_JAVA_UTIL_CONCURRENT_CANCELLATION_EXCEPTION);
        } else if (buildLog.contains(THROWABLE_POD_FAILED_WITH_STATUS_INVALID_IMAGE_NAME)) {
            return new DetectedCategory(PNC, "BUILDERS - Throwable: Pod failed with status: InvalidImageName");
        } else if (buildLog.contains(
                THE_BUILDER_POD_FAILED_TO_START_THIS_COULD_BE_DUE_TO_MISCONFIGURED_OR_BOGUS_SCRIPTS_OR_OTHER_UNKNOWN_REASONS)) {
            return new DetectedCategory(PNC, "BUILDERS - Bogus script detected during builder pod start");
        } else if (buildLog.contains(ERROR_WHILE_TRYING_TO_START_BUILDING_WITH_BPM_BUILD_SCHEDULER)) {
            if (buildLog.contains(CAUSED_BY_JAVA_NET_CONNECT_EXCEPTION_CONNECTION_REFUSED)
                    || buildLog.contains(JAVA_NET_SOCKET_EXCEPTION_UNEXPECTED_END_OF_FILE_FROM_SERVER)
                    || buildLog.contains(UNABLE_TO_RETRIEVE_CONTENT_FROM_RESPONSE)) {
                return new DetectedCategory(PNC, "RHPAM - Connection refused");
            } else if (buildLog.contains(NO_DEPLOYMENTS_AVAILABLE_FOR_COM_REDHAT_MAITAI_NCL_NCL_WORKFLOWS)) {
                return new DetectedCategory(PNC, "RHPAM - No deployments available");
            } else if (buildLog.contains(JAVA_NET_SOCKET_TIMEOUT_EXCEPTION_READ_TIMED_OUT)) {
                return new DetectedCategory(PNC, "RHPAM - Read timed out");
            } else if (buildLog.contains(CAUSED_BY_ORG_HIBERNATE_HIBERNATE_EXCEPTION)
                    || buildLog.contains(ORG_HIBERNATE_EXCEPTION_GENERIC_JDBCEXCEPTION)) {
                return new DetectedCategory(PNC, "RHPAM - Persistence exception");
            } else {
                return new DetectedCategory(PNC, "RHPAM - Error while trying to startBuilding");
            }
        } else if (buildLog.contains(CORE_EXCEPTION_ERROR_WHILE_TRYING_TO_START_BUILDING_WITH_BPM_BUILD_SCHEDULER)
                && buildLog.contains(
                        BPM_NEW_BASE_URL_HTTPS_DEVKIESERVER_NEWCASTLE_DEVEL_SERVICES_REST_SERVER_CONTAINERS)) {
            return new DetectedCategory(
                    PNC,
                    "DEVEL - Error while trying to startBuilding with BpmBuildScheduler on new RHPAM server");
        } else if (buildLog.contains(CONNECT_TO_INDY) && buildLog.contains(FAILED_CONNECTION_REFUSED)) {
            return new DetectedCategory(INDY, "INDY - Connection refused");
        } else if (buildLog.contains(CONNECT_TO_INDY) && buildLog.contains(FAILED_CONNECT_TIMED_OUT)) {
            return new DetectedCategory(INDY, "INDY - Connection timeout");
        } else if (buildLog.contains(INDY_80_FAILED_TO_RESPOND)) {
            return new DetectedCategory(INDY, "INDY - Failed to respond");
        } else if (buildLog.contains(INDY_GATEWAY_443_FAILED_TO_RESPOND)) {
            return new DetectedCategory(INDY, "INDY - Failed to respond");
        } else if (buildLog.contains(COULD_NOT_GET) && buildLog.contains(HTTP_INDY)
                && buildLog.contains(READ_TIMED_OUT_)) {
            return new DetectedCategory(INDY, "INDY - Read timed out");
        } else if (buildLog.contains(COULD_NOT_GET) && buildLog.contains(HTTPS_INDY)
                && buildLog.contains(READ_TIMED_OUT_)) {
            return new DetectedCategory(INDY, "INDY - Read timed out");
        }
        return new DetectedCategory(ND, "N.D.");
    }

    @Value
    public static class DetectedCategory {

        ArchivedBuildRecord.ErrorGroup category;
        String message;
        boolean previouslyMarkedSystemError;

        public DetectedCategory(ArchivedBuildRecord.ErrorGroup category, String message) {
            this.category = category;
            this.message = message;
            this.previouslyMarkedSystemError = false;
        }

        public DetectedCategory(
                ArchivedBuildRecord.ErrorGroup category,
                String message,
                boolean previouslyMarkedSystemError) {
            this.category = category;
            this.message = message;
            this.previouslyMarkedSystemError = previouslyMarkedSystemError;
        }
    }
}
