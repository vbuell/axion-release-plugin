package pl.allegro.tech.build.axion.release.domain.hooks

import com.github.zafarkhaja.semver.Version
import org.gradle.api.logging.Logger
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.VersionWithPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReleaseHooksRunner {

    @Inject
    private Logger logger

    @Inject
    private ScmService scmService

    @Inject
    private VersionConfig versionConfig

    void runPreReleaseHooks(VersionWithPosition versionWithPosition, Version releaseVersion) {
        HookContext context = new HookContext(logger, scmService,
                versionWithPosition.position, versionWithPosition.previousVersion, releaseVersion)
        versionConfig.hooks.preReleaseHooks.each { it.act(context) }
    }

    void runPostReleaseHooks(VersionWithPosition versionWithPosition, Version releaseVersion) {
        HookContext context = new HookContext(logger, scmService,
                versionWithPosition.position, versionWithPosition.previousVersion, releaseVersion)
        versionConfig.hooks.postReleaseHooks.each { it.act(context) }
    }
}
