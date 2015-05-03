package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.RepositoryBasedTest
import pl.allegro.tech.build.axion.release.infrastructure.di.Context

class NextVersionMarkerTest extends RepositoryBasedTest {
    
    VersionService versionService

    NextVersionMarker nextVersionMarker
    
    def setup() {
//        repository = context.repository()
//        versionService = context.versionService()
//
//        nextVersionMarker = new NextVersionMarker(context.scmService(), context.localOnlyResolver(), project.logger)
//
        Context.initContext(project)
        versionService = Context.getService(VersionService)
        nextVersionMarker = Context.getService(NextVersionMarker)
    }
    
    def "should create next version tag with given version"() {
        when:
        nextVersionMarker.markNextVersion('2.0.0')
        
        then:
        repository.currentPosition(~/.*/).latestTag == 'release-2.0.0-alpha'
        versionService.currentDecoratedVersion(config, VersionReadOptions.defaultOptions()) == '2.0.0-SNAPSHOT'
    }
}
