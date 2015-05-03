package pl.allegro.tech.build.axion.release.domain

import pl.allegro.tech.build.axion.release.RepositoryBasedTest

class VersionResolverTest extends RepositoryBasedTest {
    
    VersionResolver resolver
    
    VersionReadOptions options = VersionReadOptions.defaultOptions()
    
    def setup() {
        resolver = new VersionResolver(repository, context.versionFactory())

        resolver = builder(project).mockResolver(resolver).buildAndGet(VersionResolver)
    }
    
    def "should return default previous and current version when no tag in repository"() {
        when:
        VersionWithPosition version = resolver.resolveVersion(config, options)
        
        then:
        version.previousVersion.toString() == '0.1.0'
        version.version.toString() == '0.1.0'
        version.position.tagless()
    }
    
    def "should return same previous and current version when on release tag"() {
        given:
        repository.tag('release-1.1.0')

        when:
        VersionWithPosition version = resolver.resolveVersion(config, options)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '1.1.0'
    }
    
    def "should return unmodified previous and incremented current version when not on tag"() {
        given:
        repository.tag('release-1.1.0')
        repository.commit(['*'], 'some commit')

        when:
        VersionWithPosition version = resolver.resolveVersion(config, options)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '1.1.1'
    }
    
    def "should return previous version from last release tag and current from alpha when on alpha tag"() {
        given:
        repository.tag('release-1.1.0')
        repository.commit(['*'], 'some commit')
        repository.tag('release-2.0.0-alpha')

        when:
        VersionWithPosition version = resolver.resolveVersion(config, options)

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '2.0.0'
        !version.position.onTag
    }
    
    def "should return previous version from last release and current from forced version when forcing version"() {
        given:
        repository.tag('release-1.1.0')
        repository.commit(['*'], 'some commit')

        when:
        VersionWithPosition version = resolver.resolveVersion(config, new VersionReadOptions(true, '2.0.0'))

        then:
        version.previousVersion.toString() == '1.1.0'
        version.version.toString() == '2.0.0'
    }
}
