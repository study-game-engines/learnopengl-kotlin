package learnOpenGL.b_lighting

import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import gln.buffer.glBindBuffer
import gln.draw.glDrawArrays
import gln.get
import gln.glClearColor
import gln.glf.glf
import gln.uniform.glUniform
import gln.uniform.glUniform3f
import learnOpenGL.a_gettingStarted.end
import learnOpenGL.a_gettingStarted.swapAndPoll
import learnOpenGL.common.glEnableVertexAttribArray
import learnOpenGL.common.glVertexAttribPointer
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL30.*
import uno.buffer.destroyBuf
import uno.buffer.intBufferBig
import uno.glsl.Program

fun main() {
    with(BasicLightingDiffuse()) {
        run()
        end()
    }
}

val verticesCube0 = floatArrayOf(
    -0.5f, -0.5f, -0.5f, 0f, 0f, -1f,
    +0.5f, -0.5f, -0.5f, 0f, 0f, -1f,
    +0.5f, +0.5f, -0.5f, 0f, 0f, -1f,
    +0.5f, +0.5f, -0.5f, 0f, 0f, -1f,
    -0.5f, +0.5f, -0.5f, 0f, 0f, -1f,
    -0.5f, -0.5f, -0.5f, 0f, 0f, -1f,

    -0.5f, -0.5f, +0.5f, 0f, 0f, 1f,
    +0.5f, -0.5f, +0.5f, 0f, 0f, 1f,
    +0.5f, +0.5f, +0.5f, 0f, 0f, 1f,
    +0.5f, +0.5f, +0.5f, 0f, 0f, 1f,
    -0.5f, +0.5f, +0.5f, 0f, 0f, 1f,
    -0.5f, -0.5f, +0.5f, 0f, 0f, 1f,

    -0.5f, +0.5f, +0.5f, -1f, 0f, 0f,
    -0.5f, +0.5f, -0.5f, -1f, 0f, 0f,
    -0.5f, -0.5f, -0.5f, -1f, 0f, 0f,
    -0.5f, -0.5f, -0.5f, -1f, 0f, 0f,
    -0.5f, -0.5f, +0.5f, -1f, 0f, 0f,
    -0.5f, +0.5f, +0.5f, -1f, 0f, 0f,

    +0.5f, +0.5f, +0.5f, 1f, 0f, 0f,
    +0.5f, +0.5f, -0.5f, 1f, 0f, 0f,
    +0.5f, -0.5f, -0.5f, 1f, 0f, 0f,
    +0.5f, -0.5f, -0.5f, 1f, 0f, 0f,
    +0.5f, -0.5f, +0.5f, 1f, 0f, 0f,
    +0.5f, +0.5f, +0.5f, 1f, 0f, 0f,

    -0.5f, -0.5f, -0.5f, 0f, -1f, 0f,
    +0.5f, -0.5f, -0.5f, 0f, -1f, 0f,
    +0.5f, -0.5f, +0.5f, 0f, -1f, 0f,
    +0.5f, -0.5f, +0.5f, 0f, -1f, 0f,
    -0.5f, -0.5f, +0.5f, 0f, -1f, 0f,
    -0.5f, -0.5f, -0.5f, 0f, -1f, 0f,

    -0.5f, +0.5f, -0.5f, 0f, 1f, 0f,
    +0.5f, +0.5f, -0.5f, 0f, 1f, 0f,
    +0.5f, +0.5f, +0.5f, 0f, 1f, 0f,
    +0.5f, +0.5f, +0.5f, 0f, 1f, 0f,
    -0.5f, +0.5f, +0.5f, 0f, 1f, 0f,
    -0.5f, +0.5f, -0.5f, 0f, 1f, 0f
)

private class BasicLightingDiffuse {
    val window = initWindow0("Basic Lighting Diffuse")

    val lighting = Lighting()
    val lamp = Lamp()

    val vbo = intBufferBig(1)

    enum class VA { Cube, Light }

    val vao = intBufferBig<VA>()

    // lighting
    val lightPos = Vec3(1.2f, 1f, 2f)

    init {

        glEnable(GL_DEPTH_TEST)


        glGenVertexArrays(vao)

        // first, configure the cube's VAO (and VBO)
        glGenBuffers(vbo)

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, verticesCube0, GL_STATIC_DRAW)

        glBindVertexArray(vao[VA.Cube])

        glVertexAttribPointer(glf.pos3_nor3)
        glEnableVertexAttribArray(glf.pos3_nor3)

        // second, configure the light's VAO (VBO stays the same; the vertices are the same for the light object which is also a 3D cube)
        glBindVertexArray(vao[VA.Light])

        glBindBuffer(GL_ARRAY_BUFFER, vbo)

        // note that we update the lamp's position attribute's stride to reflect the updated buffer data
        glVertexAttribPointer(glf.pos3_nor3[0])
        glEnableVertexAttribArray(glf.pos3_nor3[0])
    }

    inner class Lighting : Lamp("shaders/b/_2_1", "basic-lighting") {

        val objCol = glGetUniformLocation(name, "objectColor")
        val lgtCol = glGetUniformLocation(name, "lightColor")
        val lgtPos = glGetUniformLocation(name, "lightPos")
    }

    open inner class Lamp(root: String = "shaders/b/_1", shader: String = "lamp") :
        Program(root, "$shader.vert", "$shader.frag") {

        val model = glGetUniformLocation(name, "model")
        val view = glGetUniformLocation(name, "view")
        val proj = glGetUniformLocation(name, "projection")
    }

    fun run() {
        while (window.open) {
            window.processFrame()

            // render
            glClearColor(clearColor0)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            // be sure to activate shader when setting uniforms/drawing objects
            glUseProgram(lighting.name)

            glUniform3f(lighting.objCol, 1f, 0.5f, 0.31f)
            glUniform3f(lighting.lgtCol, 1f)
            glUniform3f(lighting.lgtPos, lightPos)

            // view/projection transformations
            val projection = glm.perspective(camera.zoom.rad, window.aspect, 0.1f, 100f)
            val view = camera.viewMatrix
            glUniform(lighting.proj, projection)
            glUniform(lighting.view, view)

            // world transformation
            var model = Mat4()
            glUniform(lighting.model, model)

            // render the cube
            glBindVertexArray(vao[VA.Cube])
            glDrawArrays(GL_TRIANGLES, 36)

            // also draw the lamp object
            glUseProgram(lamp.name)

            glUniform(lamp.proj, projection)
            glUniform(lamp.view, view)
            model = model.translate(lightPos).scale(0.2f)
            glUniform(lamp.model, model)

            glBindVertexArray(vao[VA.Light])
            glDrawArrays(GL_TRIANGLES, 36)

            window.swapAndPoll()
        }
    }

    fun end() {
        glDeleteProgram(lighting.name)
        glDeleteProgram(lamp.name)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        destroyBuf(vao, vbo)
        window.end()
    }

}
