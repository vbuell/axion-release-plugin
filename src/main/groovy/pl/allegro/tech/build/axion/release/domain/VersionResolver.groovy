package pl.allegro.tech.build.axion.release.domain

import com.github.zafarkhaja.semver.Version
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VersionResolver {

    @Inject
    private ScmRepository repository

    @Inject
    private VersionFactory versionFactory
    
    VersionWithPosition resolveVersion(VersionConfig versionConfig, VersionReadOptions readOptions) {
        Map positions = readPositions(versionConfig)

        Version currentVersion = versionFactory.create(positions.currentPosition, versionConfig, readOptions)        
        Version previousVersion = versionFactory.create(positions.lastReleasePosition, versionConfig, VersionReadOptions.defaultOptions())

        ScmPosition position = positions.currentPosition.position
        if(positions.currentPosition.nextVersionTag) {
            position = position.asNotOnTagPosition()
        }
        
        return new VersionWithPosition(currentVersion, previousVersion, position)
    }


    private Map readPositions(VersionConfig config) {
        ScmPosition currentPosition = repository.currentPosition(~/^${config.tag.prefix}.*(|${config.nextVersion.suffix})$/)
        ScmPositionContext currentPositionContext = new ScmPositionContext(currentPosition, config.nextVersion)

        ScmPosition lastReleasePosition
        if(currentPositionContext.nextVersionTag) {
            lastReleasePosition = repository.currentPosition(~/^${config.tag.prefix}.*/, ~/.*${config.nextVersion.suffix}$/).asOnTagPosition()
        }
        else {
            lastReleasePosition = currentPosition.asOnTagPosition()
        }
        
        return [
                currentPosition: currentPositionContext,
                lastReleasePosition: new ScmPositionContext(lastReleasePosition, config.nextVersion)
        ]
    }
    
}
