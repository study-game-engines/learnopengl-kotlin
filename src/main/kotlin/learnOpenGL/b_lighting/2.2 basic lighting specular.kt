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
import gln.uniform.glUniform3
import gln.vertexArray.glVertexAttribPointer
import learnOpenGL.a_gettingStarted.end
import learnOpenGL.a_gettingStarted.swapAndPoll
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL30.*
import uno.buffer.destroyBuf
import uno.buffer.intBufferBig
import uno.glsl.Program

fun main() {
    with(BasicLightingSpecular()) {
        run()
        end()
    }
}

private class BasicLightingSpecular {
    val window = initWindow0("Basic Lighting Specular")

    val lighting = Lighting()
    val lamp = Lamp()

    val vbo = intBufferBig(1)

    enum class VA { Cube, Light }

    val vao = intBufferBig<VA>()

    val lightPos = Vec3(1.2f, 1f, 2f)

    inner class Lighting : Lamp("shaders/b/_2_2", "basic-lighting") {
        val objCol = glGetUniformLocation(name, "objectColor")
        val lgtCol = glGetUniformLocation(name, "lightColor")
        val lgtPos = glGetUniformLocation(name, "lightPos")
        val viewPos = glGetUniformLocation(name, "viewPos")
    }

    open inner class Lamp(root: String = "shaders/b/_1", shader: String = "lamp") :
        Program(root, "$shader.vert", "$shader.frag") {
        val model = glGetUniformLocation(name, "model")
        val view = glGetUniformLocation(name, "view")
        val proj = glGetUniformLocation(name, "projection")
    }

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

    fun run() {
        while (window.open) {

            window.processFrame()


            // render
            glClearColor(clearColor0)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            // be sure to activate shader when setting uniforms/drawing objects
            glUseProgram(lighting.name)

            glUniform(lighting.objCol, 1f, 0.5f, 0.31f)
            /*  we can avoid to write this
                glUniform(lighting.lgtCol, 1.0f, 1.0f, 1.0f)
                but we have to specify explicit the dimensionality with 3*/
            glUniform3(lighting.lgtCol, 1f)
            glUniform(lighting.lgtPos, lightPos)
            glUniform(lighting.viewPos, camera.position)

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
            model = model
                .translate(lightPos)
                .scale(0.2f) // a smaller cube
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
