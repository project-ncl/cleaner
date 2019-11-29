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

import javax.enterprise.context.ApplicationScoped;

/**
 * CDI bean, which manages delete operation callbacks for BUILDS and provides a blocking was of waiting fot the operation completion.
 * First the wait operation must be initiated using a method #initializeHandler and then at any time
 * a blocking method #await can be called.
 *
 * @author Jakub Bartecek
 */
@ApplicationScoped
public class BuildDeleteCallbackManager extends DeleteCallbackManager {
}
