/*
 * Copyright 2023 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.gradle.spring;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.gradle.marker.GradlePluginDescriptor;
import org.openrewrite.gradle.marker.GradleProject;
import org.openrewrite.maven.tree.MavenRepository;
import org.openrewrite.test.RewriteTest;

import java.util.Collections;
import java.util.List;

import static org.openrewrite.gradle.Assertions.buildGradle;

class AddSpringDependencyManagementPluginTest implements RewriteTest {
    @Test
    void addIfPresent() {
        GradleProject gp = gradleProject(springBootPlugin(), springDependencyManagementPlugin());
        rewriteRun(
          spec -> spec.allSources(source -> source.markers(gp))
            .recipe(new AddSpringDependencyManagementPlugin()),
          buildGradle(
            """
              plugins {
                  id "java"
                  id "org.springframework.boot" version "1.5.22.RELEASE"
              }
              
              repositories {
                  mavenCentral()
              }
              
              dependencies {
                  compile "org.springframework.boot:spring-boot-starter-web"
              }
              """,
            """
              plugins {
                  id "java"
                  id "org.springframework.boot" version "1.5.22.RELEASE"
                  id "io.spring.dependency-management" version "1.0.6.RELEASE"
              }
              
              repositories {
                  mavenCentral()
              }
              
              dependencies {
                  compile "org.springframework.boot:spring-boot-starter-web"
              }
              """
          )
        );
    }

    @Test
    void dontAddIfNotUsing() {
        GradleProject gp = gradleProject(springBootPlugin());
        rewriteRun(
          spec -> spec.allSources(source -> source.markers(gp))
            .recipe(new AddSpringDependencyManagementPlugin()),
          buildGradle(
            """
              plugins {
                  id "java"
                  id "org.springframework.boot" version "2.6.15"
              }
              
              repositories {
                  mavenCentral()
              }
              
              dependencies {
                  implementation platform("org.springframework.boot:spring-boot-starter-dependencies:2.6.15")
                  implementation "org.springframework.boot:spring-boot-starter-web"
              }
              """
          )
        );
    }

    private static GradleProject gradleProject(GradlePluginDescriptor... gradlePlugins) {
        return new GradleProject(
          Tree.randomId(),
          "example",
          ":",
          List.of(gradlePlugins),
          Collections.emptyList(),
          Collections.singletonList(MavenRepository.builder()
            .id("Gradle Central Plugin Repository")
            .uri("https://plugins.gradle.org/m2")
            .releases(true)
            .snapshots(true)
            .build()),
          Collections.emptyMap()
        );
    }

    private static GradlePluginDescriptor springBootPlugin() {
        return new GradlePluginDescriptor("org.springframework.boot.gradle.plugin.SpringBootPlugin", "org.springframework.boot");
    }

    private static GradlePluginDescriptor springDependencyManagementPlugin() {
        return new GradlePluginDescriptor("io.spring.dependencymanagement.DependencyManagementPlugin", "io.spring.dependency-management");
    }
}
