#!/bin/bash -xe
#
# JBoss, Home of Professional Open Source.
# Copyright 2019-2022 Red Hat, Inc., and individual contributors
# as indicated by the @author tags.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


set -e

repository="project-ncl/cleaner"
tagprefix=""
testparams=""
releaseparams=""

#######


if [ $# -lt 2 ]; then
	echo "You have to enter new version and RC version" >&2
	exit 1
fi

version=$1
if ! echo $version | grep -q "^[1-9][0-9]*\.[0-9]\+\.[0-9]\+$"; then
	echo "The version has to be in format X.Y.Z"
	exit 1
fi

changes=`git status --porcelain | grep -v "^??" | wc -l`
if [ $changes -gt 0 ]; then
    echo "You have uncommited changes."
    exit 1
fi


rc=$2
upstream=`git remote -v | grep "$repository" | cut -f1 | head -n1`
tag="$tagprefix$version"."$2"
majmin=`echo $version | cut -f1,2 -d.`
micro=`echo $version | cut -f3 -d.`
nextversion="$majmin.$(( micro + 1 ))-SNAPSHOT"
branch="v$majmin"


#Remove snapshot from Orch dependency
sed -i "s/<pnc.version>\([0-9].[0-9].[0-9]\)-SNAPSHOT<\/pnc.version>/<pnc.version>\1<\/pnc.version>/" pom.xml

echo "Checking out to branch $upstream/$branch"
git fetch $upstream
git checkout $upstream/$branch

echo "Testing build"
mvn clean install $testparams

echo "Seting up new version"
mvn versions:set -DnewVersion=$version
mvn versions:commit
sed -i "s/<tag>HEAD<\/tag>/<tag>$tag<\/tag>/" pom.xml

echo "Commiting changed pom files"
git add pom.xml
git commit -m "Release version $version"

echo "Deploing artifacts"
GPG_TTY=$(tty)
export GPG_TTY
mvn clean deploy -DskipTests -Prelease $releaseparams -Dgpg.executable=`which gpg2`

echo "Tagging release"
git tag $tag

#echo "Preparing for next development"
#mvn versions:set -DnewVersion=$nextversion
#mvn versions:commit
#sed -i "s/<tag>$tag<\/tag>/<tag>HEAD<\/tag>/" pom.xml
#git add pom.xml
#git commit -m "Preparing for next development"

echo
echo
echo "Release prepared. Check everything!"
echo "Then go to https://oss.sonatype.org/ and release the staging repository."
echo "When everything is done, don't forget to push:"
echo "  git push $upstream $tag"
