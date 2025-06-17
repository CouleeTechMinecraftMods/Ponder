package net.createmod.pondergradle.transformers

import dev.ithundxr.lotus.gradle.api.asm.util.IClassTransformer
import dev.ithundxr.lotus.gradle.api.asm.util.SubprojectType
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode

class EnvironmentAnnotationTransformer : IClassTransformer {
    companion object {
        private val annotations = mapOf(
            SubprojectType.FABRIC to "Lnet/fabricmc/api/Environment;",
            SubprojectType.FORGE to "Lnet/minecraftforge/api/distmarker/OnlyIn;",
            SubprojectType.NEOFORGE to "Lnet/neoforged/api/distmarker/OnlyIn;",
        )

        private val annotationValues = mapOf(
            SubprojectType.FABRIC to Triple("Lnet/fabricmc/api/EnvType;", "CLIENT", "SERVER"),
            SubprojectType.FORGE to Triple("Lnet/minecraftforge/api/distmarker/Dist;", "CLIENT", "DEDICATED_SERVER"),
            SubprojectType.NEOFORGE to Triple("Lnet/neoforged/api/distmarker/Dist;", "CLIENT", "DEDICATED_SERVER"),
        )
    }

    override fun transform(project: SubprojectType, node: ClassNode) {
        node.visibleAnnotations = node.visibleAnnotations ?: mutableListOf()
        node.invisibleAnnotations = node.invisibleAnnotations ?: mutableListOf()
        process(project, node.visibleAnnotations, node.invisibleAnnotations)

        for (fieldNode in node.fields) {
            fieldNode.visibleAnnotations = fieldNode.visibleAnnotations ?: mutableListOf()
            fieldNode.invisibleAnnotations = fieldNode.invisibleAnnotations ?: mutableListOf()
            process(project, fieldNode.visibleAnnotations, fieldNode.invisibleAnnotations)
        }

        for (methodNode in node.methods) {
            methodNode.visibleAnnotations = methodNode.visibleAnnotations ?: mutableListOf()
            methodNode.invisibleAnnotations = methodNode.invisibleAnnotations ?: mutableListOf()
            process(project, methodNode.visibleAnnotations, methodNode.invisibleAnnotations)
        }
    }

    private fun process(project: SubprojectType, visibleAnnotations: MutableList<AnnotationNode>, invisibleAnnotations: MutableList<AnnotationNode>) {
        val allAnnotationNodes = mutableListOf<AnnotationNode>()
        visibleAnnotations.let { allAnnotationNodes.addAll(it) }
        invisibleAnnotations.let { allAnnotationNodes.addAll(it) }

        val annotationsList = if (project == SubprojectType.FABRIC) invisibleAnnotations else visibleAnnotations
        val descriptor = annotations[project]!!
        val (enumDesc, clientValue, serverValue) = annotationValues[project]!!

        for (annotation in allAnnotationNodes) {
            if (annotation.desc == "Lnet/createmod/catnip/annotations/Environment;") {
                val value = if ((annotation.values[1] as Array<*>)[1] == "CLIENT") clientValue else serverValue

                val newAnnotation = AnnotationNode(descriptor)
                newAnnotation.values = mutableListOf<Any>()
                newAnnotation.values.add("value")
                newAnnotation.values.add(arrayOf(enumDesc, value))

                annotationsList.add(newAnnotation)
                invisibleAnnotations.remove(annotation)
            }
        }
    }
}
