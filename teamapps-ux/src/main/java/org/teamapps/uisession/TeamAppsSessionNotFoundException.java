/*-
 * ========================LICENSE_START=================================
 * TeamApps
 * ---
 * Copyright (C) 2014 - 2021 TeamApps.org
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.teamapps.uisession;

public class TeamAppsSessionNotFoundException extends RuntimeException {

	private final QualifiedUiSessionId sessionId;

	public TeamAppsSessionNotFoundException(QualifiedUiSessionId sessionId) {
		super("Could not find TeamApps session: " + sessionId.toString());
		this.sessionId = sessionId;
	}

	public QualifiedUiSessionId getSessionId() {
		return sessionId;
	}
}
