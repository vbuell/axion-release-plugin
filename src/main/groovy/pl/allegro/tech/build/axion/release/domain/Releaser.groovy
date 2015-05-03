package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHooksRunner
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Releaser {

    @Inject
    private ScmService repository

    @Inject
    private ReleaseHooksRunner hooksRunner

    @Inject
    private LocalOnlyResolver localOnlyResolver

    @Inject
    private Logger logger

    @Inject
    private Project project

    void release(VersionConfig versionConfig) {
        VersionWithPosition positionedVersion = versionConfig.getRawVersion()
        Version version = positionedVersion.version

        if (notOnTagAlready(positionedVersion) || VersionReadOptions.fromProject(project).forceVersion) {
            String tagName = versionConfig.tag.serialize(versionConfig.tag, version.toString())

            if (versionConfig.createReleaseCommit) {
                logger.quiet("Creating release commit")
                versionConfig.hooks.pre('commit', versionConfig.releaseCommitMessage)
            }

            hooksRunner.runPreReleaseHooks(positionedVersion, version)

            logger.quiet("Creating tag: $tagName")
            repository.tag(tagName)

            hooksRunner.runPostReleaseHooks(positionedVersion, version)
        } else {
            logger.quiet("Working on released version ${versionConfig.version}, nothing to release.")
        }
    }

    void pushRelease(VersionConfig versionConfig) {
        if (!localOnlyResolver.localOnly(repository.remoteAttached())) {
            repository.push()
        } else {
            logger.quiet("Changes made to local repository only")
        }

    }

    private boolean notOnTagAlready(VersionWithPosition positionedVersion) {
        return positionedVersion.snapshotVersion
    }
}
