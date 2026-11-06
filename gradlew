#!/usr/bin/env sh

#
# Copyright © 2015-2021 the original authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

if [ -n "$DEBUG" ] ; then
    echo "Diagnostics: $arg"
fi

app_name="Gradle"
app_home=$( dirname "$( readlink -f -- "$0"; )" )

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/bin/sh" ] ; then
        JAVACMD="$JAVA_HOME/bin/sh"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || { echo >&2 "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."; exit 1; }
fi

# Catch the `-D...` options and pass them to the JVM
exit_status=1
exec "$JAVACMD" -jar "$app_home/gradle/wrapper/gradle-wrapper.jar" "$@"
