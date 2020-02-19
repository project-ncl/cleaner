/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.cleaner.temporaryBuilds;

import org.jboss.pnc.spi.exception.ValidationException;

/**
 * Interface for schedulers to delete builds without a transaction context
 *
 * @author Jakub Bartecek
 */
public interface TemporaryBuildsCleaner {

    /**
     * Cleanup old temporary builds
     * @throws ValidationException Thrown if the cleanup fails with error
     */
    void cleanupExpiredTemporaryBuilds() throws ValidationException;
}
