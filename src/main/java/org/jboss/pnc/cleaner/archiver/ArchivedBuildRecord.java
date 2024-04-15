/**
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

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.annotations.Type;
import org.jboss.pnc.enums.BuildStatus;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.time.Instant;

@Table(name = "_archived_buildrecords")
@Entity
public class ArchivedBuildRecord extends PanacheEntityBase {
    public enum ErrorGroup {
        PNC, PSI, INDY, ND
    }

    @Id
    @Column(name = "buildrecord_id")
    Long buildRecordId;

    @Column(name = "submittime")
    Instant submitTime;

    @Column(name = "starttime")
    Instant startTime;

    @Column(name = "endtime")
    Instant endTime;

    @Column(name = "submit_year")
    int submitYear;

    @Column(name = "submit_month")
    int submitMonth;

    @Column(name = "submit_quarter")
    int submitQuarter;

    @Column(name = "status")
    BuildStatus status;

    @Column(name = "buildtype", length = 100)
    String buildType;

    @Column(name = "executionrootname")
    String executionRootName;

    @Column(name = "executionrootversion", length = 100)
    String executionRootVersion;

    @Column(name = "user_id")
    long userId;

    @Column(name = "username", length = 50)
    String username;

    @Column(name = "temporarybuild")
    boolean temporaryBuild;

    @Column(name = "autoalign")
    boolean autoAlign;

    @Column(name = "categorized_error_msg")
    String categorizedErrorMessage;

    @Column(name = "categorized_error_group")
    ErrorGroup categorizedErrorGroup;

    @Column(name = "brewpullactive")
    boolean brewPullActive;

    @Column(name = "buildconfiguration_id")
    long buildConfigID;

    @Column(name = "buildconfiguration_rev")
    long buildConfigRev;

    @Column(name = "buildconfiguration_name")
    String buildConfigName;

    @Column(name = "buildenvironment_id")
    long buildEnvironmentID;

    @Column(name = "project_id")
    long projectID;

    @Column(name = "project_name")
    String projectName;

    @Column(name = "buildconfigsetrecord_id")
    Long groupBuildID;

    @Column(name = "product_id")
    Long productID;

    @Column(name = "product_name")
    String productName;

    @Column(name = "productversion_id")
    Long productVersionID;

    @Column(name = "product_version", length = 50)
    String productVersion;

    @Column(name = "productmilestone_id")
    Long productMilestoneID;

    @Column(name = "productmilestone_version", length = 50)
    String productMilestoneVersion;

    @Column(name = "buildcontentid", length = 50)
    String buildcontentID;

    @Column(name = "trimmed_buildlog")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Basic(fetch = FetchType.LAZY)
    String trimmedBuildLog;

    @Column(name = "trimmed_repourlog")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Basic(fetch = FetchType.LAZY)
    String trimmedAlignLog;

    @Column(name = "lastupdatetime")
    Instant lastUpdate;

}
