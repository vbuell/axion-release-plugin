package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.Project

import javax.annotation.PostConstruct
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalOnlyResolver {

    private static final String LOCAL_ONLY = "release.localOnly"

    @Inject
    private Project project

    @Inject
    private VersionConfig config

    private boolean localOnly

    @PostConstruct
    private void postConstruct() {
        this.localOnly = config.localOnly
    }

    boolean localOnly(boolean remoteAttached) {
        if (project.hasProperty(LOCAL_ONLY)) {
            return true
        }
        if(localOnly) {
            return true
        }
        return !remoteAttached
    }
}
