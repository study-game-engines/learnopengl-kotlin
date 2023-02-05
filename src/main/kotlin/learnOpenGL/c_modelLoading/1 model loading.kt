package learnOpenGL.c_modelLoading

import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import gln.glf.semantic
import gln.program.usingProgram
import gln.uniform.glUniform
import learnOpenGL.a_gettingStarted.end
import learnOpenGL.a_gettingStarted.swapAndPoll
import learnOpenGL.b_lighting.camera
import learnOpenGL.b_lighting.initWindow0
import learnOpenGL.b_lighting.processFrame
import learnOpenGL.common.Model
import org.lwjgl.opengl.GL20.*
import uno.glsl.Program

fun main() {
    with(ModelLoading()) {
        run()
        end()
    }
}

private class ModelLoading {
    val window = initWindow0("Model Loading")

    val program = ProgramA()

    val ourModel: Model

    inner class ProgramA : Program("shaders/c/_1", "model-loading.vert", "model-loading.frag") {
        val model = glGetUniformLocation(name, "model")
        val view = glGetUniformLocation(name, "view")
        val proj = glGetUniformLocation(name, "projection")

        init {
            usingProgram(name) { "texture_diffuse".unit = semantic.sampler.DIFFUSE }
        }
    }

    init {
        glEnable(GL_DEPTH_TEST)
        ourModel = Model("objects/nanosuit/nanosuit.obj")
    }

    fun run() {
        while (window.open) {
            window.processFrame()

            // render
            glClearColor(0.05f, 0.05f, 0.05f, 1f)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            // don't forget to enable shader before setting uniforms
            glUseProgram(program.name)

            // view/projection transformations
            val projection = glm.perspective(camera.zoom.rad, window.aspect, 0.1f, 100.0f)
            val view = camera.viewMatrix
            glUniform(program.proj, projection)
            glUniform(program.view, view)

            // render the loaded model
            val model = Mat4().translate(0f, -1.75f, 0f).scale(0.2f)
            glUniform(program.model, model)
            ourModel.draw(diffuse = true)

            window.swapAndPoll()
        }
    }

    fun end() {
        glDeleteProgram(program.name)
        ourModel.dispose()
        window.end()
    }

}
