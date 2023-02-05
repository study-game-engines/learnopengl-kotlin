package learnOpenGL.d_advancedOpenGL

import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.set
import gln.draw.glDrawArrays
import gln.get
import gln.glClearColor
import gln.glf.glf
import gln.glf.semantic
import gln.program.usingProgram
import gln.uniform.glUniform
import gln.vertexArray.glBindVertexArray
import gln.vertexArray.glVertexAttribPointer
import gln.vertexArray.withVertexArray
import learnOpenGL.a_gettingStarted.end
import learnOpenGL.a_gettingStarted.swapAndPoll
import learnOpenGL.a_gettingStarted.verticesCube
import learnOpenGL.b_lighting.camera
import learnOpenGL.b_lighting.clearColor0
import learnOpenGL.b_lighting.initWindow0
import learnOpenGL.b_lighting.processFrame
import learnOpenGL.common.loadCubemap
import learnOpenGL.common.loadTexture
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL30.*
import uno.buffer.destroyBuf
import uno.buffer.intBufferBig
import uno.glsl.Program

fun main() {
    with(CubemapsSkybox()) {
        run()
        end()
    }
}

val verticesSkybox = floatArrayOf(
    // positions
    -1f, +1f, -1f,
    -1f, -1f, -1f,
    +1f, -1f, -1f,
    +1f, -1f, -1f,
    +1f, +1f, -1f,
    -1f, +1f, -1f,

    -1f, -1f, +1f,
    -1f, -1f, -1f,
    -1f, +1f, -1f,
    -1f, +1f, -1f,
    -1f, +1f, +1f,
    -1f, -1f, +1f,

    +1f, -1f, -1f,
    +1f, -1f, +1f,
    +1f, +1f, +1f,
    +1f, +1f, +1f,
    +1f, +1f, -1f,
    +1f, -1f, -1f,

    -1f, -1f, +1f,
    -1f, +1f, +1f,
    +1f, +1f, +1f,
    +1f, +1f, +1f,
    +1f, -1f, +1f,
    -1f, -1f, +1f,

    -1f, +1f, -1f,
    +1f, +1f, -1f,
    +1f, +1f, +1f,
    +1f, +1f, +1f,
    -1f, +1f, +1f,
    -1f, +1f, -1f,

    -1f, -1f, -1f,
    -1f, -1f, +1f,
    +1f, -1f, -1f,
    +1f, -1f, -1f,
    -1f, -1f, +1f,
    +1f, -1f, +1f
)

private class CubemapsSkybox {
    val window = initWindow0("Cubemaps Skybox")

    val program = ProgramA()
    val skyboxProgram = ProgramSkybox()

    enum class Object { Cube, Skybox }

    val vao = intBufferBig<Object>()
    val vbo = intBufferBig<Object>()
    val tex = intBufferBig<Object>()

    open inner class ProgramA : ProgramSkybox("cubemaps") {
        val model = glGetUniformLocation(name, "model")
    }

    open inner class ProgramSkybox(shader: String = "skybox") :
        Program("shaders/d/_6_1", "$shader.vert", "$shader.frag") {

        val view = glGetUniformLocation(name, "view")
        val proj = glGetUniformLocation(name, "projection")

        init {
            usingProgram(name) { "texture1".unit = semantic.sampler.DIFFUSE }
        }

    }

    init {
        glEnable(GL_DEPTH_TEST)
        glGenVertexArrays(vao)
        glGenBuffers(vbo)

        for (i in Object.values()) {
            glBindVertexArray(vao[i])
            glBindBuffer(GL_ARRAY_BUFFER, vbo[i])
            if (i == Object.Cube) {
                glBufferData(GL_ARRAY_BUFFER, verticesCube, GL_STATIC_DRAW)
                glEnableVertexAttribArray(glf.pos3_tc2)
                glVertexAttribPointer(glf.pos3_tc2)
            } else {
                glBufferData(GL_ARRAY_BUFFER, verticesSkybox, GL_STATIC_DRAW)
                glEnableVertexAttribArray(glf.pos3)
                glVertexAttribPointer(glf.pos3)
            }
            glBindVertexArray()
        }

        // load textures
        tex[Object.Cube.ordinal] = loadTexture("textures/marble.jpg")
        tex[Object.Skybox.ordinal] = loadCubemap("textures/skybox", "jpg")
    }


    fun run() {

        while (window.open) {

            window.processFrame()


            glClearColor(clearColor0)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            // draw scene as normal
            glUseProgram(program.name)
            val model = Mat4()
            val view = camera.viewMatrix
            val projection = glm.perspective(camera.zoom.rad, window.aspect, 0.1f, 100f)
            glUniform(program.model, model)
            glUniform(program.view, view)
            glUniform(program.proj, projection)
            // cubes
            withVertexArray(vao[Object.Cube]) {
                glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE)
                glBindTexture(GL_TEXTURE_2D, tex[Object.Cube])
                glDrawArrays(GL_TRIANGLES, 36)
            }

            // draw skybox as last
            glDepthFunc(GL_LEQUAL)  // change depth function so depth test passes when values are equal to depth buffer's content
            glUseProgram(skyboxProgram.name)
            view put camera.viewMatrix.toMat3().toMat4() // remove translation from the view matrix
            glUniform(skyboxProgram.view, view)
            glUniform(skyboxProgram.proj, projection)
            // skybox cube
            withVertexArray(vao[Object.Skybox]) {
                glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE)
                glBindTexture(GL_TEXTURE_CUBE_MAP, tex[Object.Skybox])
                glDrawArrays(GL_TRIANGLES, 36)
            }
            glDepthFunc(GL_LESS) // set depth function back to default


            window.swapAndPoll()
        }
    }

    fun end() {
        glDeleteProgram(program.name)
        glDeleteProgram(skyboxProgram.name)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        glDeleteTextures(tex)
        destroyBuf(vao, vbo, tex)
        window.end()
    }

}