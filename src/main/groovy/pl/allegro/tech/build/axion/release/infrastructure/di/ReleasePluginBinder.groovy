package pl.allegro.tech.build.axion.release.infrastructure.di;

import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import pl.allegro.tech.build.axion.release.domain.LocalOnlyResolver
import pl.allegro.tech.build.axion.release.domain.NextVersionMarker
import pl.allegro.tech.build.axion.release.domain.Releaser
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.VersionDecorator
import pl.allegro.tech.build.axion.release.domain.VersionFactory
import pl.allegro.tech.build.axion.release.domain.VersionResolver
import pl.allegro.tech.build.axion.release.domain.VersionSanitizer
import pl.allegro.tech.build.axion.release.domain.VersionService
import pl.allegro.tech.build.axion.release.domain.hooks.ReleaseHooksRunner
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository
import pl.allegro.tech.build.axion.release.domain.scm.ScmService
import pl.allegro.tech.build.axion.release.infrastructure.DryRepository
import pl.allegro.tech.build.axion.release.infrastructure.GradleAwareScmService;

public class ReleasePluginBinder extends AbstractBinder {

    Project project

    ReleasePluginBinder(Project project) {
        this.project = project
    }

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
        this.addActiveDescriptor(VersionResolver)
        this.addActiveDescriptor(VersionService)
        this.addActiveDescriptor(VersionFactory)
        this.addActiveDescriptor(VersionSanitizer)
        this.addActiveDescriptor(VersionDecorator)
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
