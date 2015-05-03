package pl.allegro.tech.build.axion.release.domain

import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.hk2.utilities.ServiceLocatorUtilities
import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHooksRunner
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.domain.scm.ScmService
import pl.allegro.tech.build.axion.release.infrastructure.DryRepository
import pl.allegro.tech.build.axion.release.infrastructure.GradleAwareScmService
import pl.allegro.tech.build.axion.release.infrastructure.di.ScmRepositoryFactory
import spock.lang.Specification

class Hk2BasedTest extends Specification {

    ServiceLocator sl;

    TestContextBuilder builder(Project project) {
        return new TestContextBuilder(project)
    }

    void shutdown() {
        sl.shutdown()
    }

    class TestContextBuilder {

        VersionResolver versionResolver = null;

        Project project;

        TestContextBuilder builder(Project project) {
            return new TestContextBuilder(project)
        }

        public TestContextBuilder(Project project) {
            this.project = project
        }

        public TestContextBuilder mockResolver(VersionResolver versionResolver) {
            this.versionResolver = versionResolver
            return this
        }

        public <T> T buildAndGet(Class<T> serviceClass) {
            ServiceLocator sl = build()
            return sl.getService(serviceClass)
        }

        public ServiceLocator build() {
            ServiceLocator sl = ServiceLocatorUtilities.bind("", new AbstractBinder() {
                @Override
                protected void configure() {
                    // Bind Project itself and VersionConfig for handy accessing
                    this.bind(project).to(Project)
                    this.bind(project.logger).to(Logger)
                    this.bind(config()).to(VersionConfig)

                    // Dynamically configure implentations
                    ScmRepository scmRepository = repository()
                    this.bind(scmService(scmRepository)).to(ScmService)
                    this.bind(scmRepository).to(ScmRepository)

                    this.addActiveDescriptor(Releaser)
                    this.addActiveDescriptor(LocalOnlyResolver)
                    this.addActiveDescriptor(ReleaseHooksRunner)
                    this.addActiveDescriptor(NextVersionMarker)
                    if (versionResolver != null) {
                        this.bind(versionResolver).to(VersionResolver)
                    }
                    else {
                        this.addActiveDescriptor(VersionResolver)
                    }
                    this.addActiveDescriptor(VersionService)
                    this.addActiveDescriptor(VersionFactory)
                    this.addActiveDescriptor(VersionSanitizer)
                    this.addActiveDescriptor(VersionDecorator)
                }
            })
            Hk2BasedTest.this.sl = sl
            return sl
        }

        private VersionConfig config() {
            return project.extensions.getByType(VersionConfig)
        }

        private ScmRepository repository() {
            ScmRepository scmRepository = new ScmRepositoryFactory().create(project, config().repository)
            return config().dryRun ? new DryRepository(scmRepository, project.logger) : scmRepository
        }

        private ScmService scmService(ScmRepository scmRepository) {
            return new GradleAwareScmService(project, config().repository, scmRepository)
        }


    }

}
