package org.eclipse.buildship.core.preferences.internal

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.preferences.ModelPersistence
import org.eclipse.buildship.core.preferences.PersistentModel
import org.eclipse.buildship.core.preferences.PersistentModelBuilder
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification

class DefaultModelPersistenceTest extends WorkspaceSpecification {

    IProject project

    void setup() {
        project = newProject('sample-project')
    }

    def "By default an empty model is returned"() {
        setup:
        PersistentModel model = CorePlugin.modelPersistence().loadModel(project)

        expect:
        model.project == project
        model.emptyModel
        model.buildDir == new Path('build')
        model.subprojectPaths.isEmpty()
        model.classpath.isEmpty()
        model.derivedResources.isEmpty()
        model.linkedResources.isEmpty()
    }

    def "Can store and load a model"() {
        setup:
        def buildDir = new Path('buildDir')
        def subProjectPaths = [new Path('subproject')]
        def classpath = [JavaCore.newProjectEntry(new Path('/project-path'))]
        def derivedResources = [new Path('derived')]
        def linkedResources = [project.getFolder('linked')]

        PersistentModel model = PersistentModel.builder(project)
            .buildDir(buildDir)
            .subprojectPaths(subProjectPaths)
            .classpath(classpath)
            .derivedResources(derivedResources)
            .linkedResources(linkedResources)
            .build()

        when:
        CorePlugin.modelPersistence().saveModel(model)
        model = CorePlugin.modelPersistence().loadModel(project)

        then:
        model.project == project
        !model.emptyModel
        model.buildDir == buildDir
        model.subprojectPaths == subProjectPaths
        model.classpath == classpath
        model.derivedResources == derivedResources
        model.linkedResources == linkedResources
    }

    def "Can delete a model"() {
        setup:
        def buildDir = new Path('buildDir')
        def subProjectPaths = [new Path('subproject')]
        def classpath = [JavaCore.newProjectEntry(new Path('/project-path'))]
        def derivedResources = [new Path('derived')]
        def linkedResources = [project.getFolder('linked')]

        PersistentModel model = PersistentModel.builder(project)
            .buildDir(buildDir)
            .subprojectPaths(subProjectPaths)
            .classpath(classpath)
            .derivedResources(derivedResources)
            .linkedResources(linkedResources)
            .build()
        CorePlugin.modelPersistence().saveModel(model)

        when:
        CorePlugin.modelPersistence().deleteModel(project)
        model = CorePlugin.modelPersistence().loadModel(project)

        then:
        model.project == project
        model.isEmptyModel
        model.buildDir == new Path('build')
        model.subprojectPaths.isEmpty()
        model.classpath.isEmpty()
        model.derivedResources.isEmpty()
        model.linkedResources.isEmpty()
    }

    def "Model is still accessible if the referenced project is renamed"() {
        setup:
        def buildDir = new Path('buildDir')
        def subProjectPaths = [new Path('subproject')]
        def classpath = [JavaCore.newProjectEntry(new Path('/project-path'))]
        def derivedResources = [new Path('derived')]
        def linkedResources = [project.getFolder('linked')]

        PersistentModel model = PersistentModel.builder(project)
            .buildDir(buildDir)
            .subprojectPaths(subProjectPaths)
            .classpath(classpath)
            .derivedResources(derivedResources)
            .linkedResources(linkedResources)
            .build()
        CorePlugin.modelPersistence().saveModel(model)

        when:
        project = CorePlugin.workspaceOperations().renameProject(project, 'new-project-name', new NullProgressMonitor())
        model = CorePlugin.modelPersistence().loadModel(project)

        then:
        !model.emptyModel
        model.buildDir == buildDir
        model.subprojectPaths == subProjectPaths
        model.classpath == classpath
        model.derivedResources == derivedResources
        model.linkedResources == linkedResources
    }
}
