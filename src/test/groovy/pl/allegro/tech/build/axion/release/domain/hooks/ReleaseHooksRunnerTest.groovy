package pl.allegro.tech.build.axion.release.domain.hooks

import com.github.zafarkhaja.semver.Version
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import pl.allegro.tech.build.axion.release.domain.Hk2BasedTest
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.VersionService
import pl.allegro.tech.build.axion.release.domain.VersionWithPosition
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition
import spock.lang.Specification

class ReleaseHooksRunnerTest extends Hk2BasedTest {
    
    private HooksConfig config = new HooksConfig()
    
    private ReleaseHooksRunner runner

    private VersionWithPosition version = new VersionWithPosition(
            new Version.Builder().setNormalVersion('2.0.0-SNAPSHOT').build(),
            new Version.Builder().setNormalVersion('1.0.0').build(),
            new ScmPosition('master', 'release-1.0.0', false)
    )
    
    private Version releaseVersion = new Version.Builder().setNormalVersion('2.0.0').build()

    def setup() {
        Project project = ProjectBuilder.builder().build()

        VersionConfig versionConfig = project.extensions.create('versionConfig', VersionConfig, project)
        versionConfig.hooks = config

        runner = builder(project).buildAndGet(ReleaseHooksRunner)
    }

    def cleanup() {
        shutdown()
    }

    def "should run all pre-release jobs"() {
        given:
        int preReleaseRun = 0
        Map versions = [:]
        config.pre { c -> preReleaseRun++ }
        config.pre { c -> preReleaseRun++; versions.previous = c.previousVersion; versions.current = c.currentVersion }

        when:
        runner.runPreReleaseHooks(version, releaseVersion)
        
        then:
        preReleaseRun == 2
        versions == [previous: '1.0.0', current: '2.0.0']
    }

    def "should run all post-release jobs"() {
        given:
        int postReleaseRun = 0
        config.post { postReleaseRun++ }
        config.post { postReleaseRun++ }

        when:
        runner.runPostReleaseHooks(version, version.version)

        then:
        postReleaseRun == 2
    }
}
