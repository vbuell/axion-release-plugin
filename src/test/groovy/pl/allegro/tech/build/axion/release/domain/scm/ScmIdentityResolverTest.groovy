package pl.allegro.tech.build.axion.release.domain.scm

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.domain.RepositoryConfig
import spock.lang.Specification

class ScmIdentityResolverTest extends Specification {

    def "should return default identity when no key set"() {
        given:
        RepositoryConfig config = new RepositoryConfig()

        when:
        ScmIdentity identity = ScmIdentityResolver.resolve(config)

        then:
        identity.useDefault
    }

    def "should return custom identity when key set"() {
        given:
        RepositoryConfig config = new RepositoryConfig(customKey: 'key', customKeyPassword: 'password')

        when:
        ScmIdentity identity = ScmIdentityResolver.resolve(config)

        then:
        !identity.useDefault
        identity.privateKey == 'key'
        identity.passPhrase == 'password'
    }

    def "should read key contents from file when file passed as key"() {
        given:
        Project project = ProjectBuilder.builder().build()

        File keyFile = project.file('./keyFile')
        keyFile.createNewFile()
        keyFile << 'keyFile'

        RepositoryConfig config = new RepositoryConfig(customKey: keyFile, customKeyPassword: 'password')

        when:
        ScmIdentity identity = ScmIdentityResolver.resolve(config)

        then:
        !identity.useDefault
        identity.privateKey == 'keyFile'
        identity.passPhrase == 'password'
    }
}
