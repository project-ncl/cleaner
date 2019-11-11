/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.cleaner.orchapi.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jboss.pnc.model.BuildStatus;

import java.util.Date;
import java.util.Set;

@ToString
public class BuildConfigSetRecordRest {

    private Integer id;

    private Integer buildConfigurationSetId;

    private String buildConfigurationSetName;

    private Long startTime;

    private Long endTime;

    private org.jboss.pnc.model.BuildStatus status;

    private Integer userId;

    private String username;

    private Integer productVersionId;

    private Set<Integer> buildRecordIds;

    @Getter
    @Setter(onMethod = @__({@Deprecated}))
    private Boolean temporaryBuild;

    public BuildConfigSetRecordRest() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public org.jboss.pnc.model.BuildStatus getStatus() {
        return status;
    }

    public void setStatus(BuildStatus status) {
        this.status = status;
    }

    public Integer getBuildConfigurationSetId() {
        return buildConfigurationSetId;
    }

    public void setBuildConfigurationSetId(Integer buildConfigurationSetId) {
        this.buildConfigurationSetId = buildConfigurationSetId;
    }

    public String getBuildConfigurationSetName() {
        return buildConfigurationSetName;
    }

    public void setBuildConfigurationSetName(String buildConfigurationSetName) {
        this.buildConfigurationSetName = buildConfigurationSetName;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getProductVersionId() {
        return productVersionId;
    }

    public void setProductVersionId(Integer productVersionId) {
        this.productVersionId = productVersionId;
    }

    public Set<Integer> getBuildRecordIds() {
        return buildRecordIds;
    }
}
