package pl.allegro.tech.build.axion.release.domain

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VersionService {

    static final String SNAPSHOT = "SNAPSHOT"

    @Inject
    private VersionDecorator versionDecorator

    @Inject
    private VersionResolver versionResolver

    @Inject
    private VersionSanitizer sanitizer

    VersionWithPosition currentVersion(VersionConfig versionConfig, VersionReadOptions options) {
        VersionWithPosition positionedVersion = versionResolver.resolveVersion(versionConfig, options)

        if (!positionedVersion.position.onTag) {
            positionedVersion.asSnapshotVersion()
        }

        return positionedVersion
    }

    String currentDecoratedVersion(VersionConfig versionConfig, VersionReadOptions options) {
        VersionWithPosition positionedVersion = versionResolver.resolveVersion(versionConfig, options)
        String version = versionDecorator.createVersion(versionConfig, positionedVersion)

        if (versionConfig.sanitizeVersion) {
            version = sanitizer.sanitize(version)
        }

        if (!positionedVersion.position.onTag) {
            version = version + '-' + SNAPSHOT
        }

        return version
    }
}
