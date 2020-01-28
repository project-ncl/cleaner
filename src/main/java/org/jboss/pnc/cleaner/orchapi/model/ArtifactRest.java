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

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.jboss.pnc.model.Artifact.Quality;

import java.util.Date;
import java.util.Set;

public class ArtifactRest {

    private Integer id;

    private String identifier;

    private Quality artifactQuality;

    private TargetRepositoryRest targetRepository;

    @Getter
    @Setter
    private String md5;

    @Getter
    @Setter
    private String sha1;

    @Getter
    @Setter
    private String sha256;

    private String filename;

    private String deployPath;

    private Set<Integer> buildRecordIds;

    private Set<Integer> dependantBuildRecordIds;

    private Date importDate;

    private String originUrl;

    private Long size;

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    /**
     * Internal url to the artifact using internal (cloud) network domain
     */
    @Getter
    @Setter
    private String deployUrl;

    /**
     * Public url to the artifact using public network domain
     */
    @Getter
    @Setter
    private String publicUrl;

    public ArtifactRest() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public TargetRepositoryRest getTargetRepository() {
        return targetRepository;
    }

    public void setTargetRepository(TargetRepositoryRest targetRepository) {
        this.targetRepository = targetRepository;
    }

    public Quality getArtifactQuality() {
        return artifactQuality;
    }

    public void setArtifactQuality(Quality artifactQuality) {
        this.artifactQuality = artifactQuality;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDeployPath() {
        return deployPath;
    }

    public void setDeployPath(String deployPath) {
        this.deployPath = deployPath;
    }

    public Date getImportDate() {
        return importDate;
    }

    public void setImportDate(Date importDate) {
        this.importDate = importDate;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    @JsonIgnore
    public boolean isImported() {
        return (originUrl != null && !originUrl.isEmpty());
    }

    @Deprecated
    @JsonIgnore
    public String getStatus() {
        if (buildRecordIds != null && buildRecordIds.size() > 0) {
            return "BINARY_BUILT";
        }
        return "BINARY_IMPORTED";
    }

    public Set<Integer> getBuildRecordIds() {
        return buildRecordIds;
    }

    public void setBuildRecordIds(Set<Integer> buildRecordIds) {
        this.buildRecordIds = buildRecordIds;
    }

    @JsonIgnore
    public boolean isBuilt() {
        return (buildRecordIds != null && buildRecordIds.size() > 0);
    }

    public Set<Integer> getDependantBuildRecordIds() {
        return dependantBuildRecordIds;
    }

    public void setDependantBuildRecordIds(Set<Integer> dependantBuildRecordIds) {
        this.dependantBuildRecordIds = dependantBuildRecordIds;
    }

    @Override
    public String toString() {
        return "ArtifactRest{" + "id=" + id + ", identifier='" + identifier + '\'' + ", artifactQuality=" + artifactQuality
                + ", targetRepository=" + targetRepository.toString() + ", md5='" + md5 + '\'' + ", sha1='" + sha1 + '\''
                + ", sha256='" + sha256 + '\'' + ", filename='" + filename + '\'' + ", deployPath='" + deployPath + '\''
                + ", buildRecordIds=" + buildRecordIds + ", dependantBuildRecordIds=" + dependantBuildRecordIds
                + ", importDate=" + importDate + ", originUrl='" + originUrl + '\'' + ", deployUrl='" + deployUrl + '\''
                + ", publicUrl='" + publicUrl + '\'' + '}';
    }
}
