package pl.allegro.tech.build.axion.release.domain

import org.gradle.api.logging.Logger
import pl.allegro.tech.build.axion.release.domain.scm.ScmService

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NextVersionMarker {

    @Inject
    private ScmService repository

    @Inject
    private LocalOnlyResolver localOnlyResolver

    @Inject
    private Logger logger

    @Inject
    private VersionConfig versionConfig

    void markNextVersion(String nextVersion) {
        String tagName = versionConfig.tag.serialize(versionConfig.tag, nextVersion)
        String nextVersionTag = versionConfig.nextVersion.serializer(versionConfig.nextVersion, tagName)

        logger.quiet("Creating next version marker tag: $nextVersionTag")
        repository.tag(nextVersionTag)

        if(!localOnlyResolver.localOnly(repository.remoteAttached())) {
            repository.push()
        }
        else {
            logger.quiet("Changes made to local repository only")
        }
    }
}
