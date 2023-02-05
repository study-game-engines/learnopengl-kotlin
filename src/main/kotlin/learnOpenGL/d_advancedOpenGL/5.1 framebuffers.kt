package learnOpenGL.d_advancedOpenGL

import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.set
import gln.draw.glDrawArrays
import gln.framebuffer.glFramebufferRenderbuffer
import gln.get
import gln.glClearColor
import gln.glf.glf
import gln.glf.semantic
import gln.program.usingProgram
import gln.renderbuffer.glBindRenderbuffer
import gln.renderbuffer.glRenderbufferStorage
import gln.uniform.glUniform
import gln.vertexArray.glBindVertexArray
import learnOpenGL.a_gettingStarted.end
import learnOpenGL.a_gettingStarted.swapAndPoll
import learnOpenGL.a_gettingStarted.windowSize
import learnOpenGL.b_lighting.camera
import learnOpenGL.b_lighting.clearColor0
import learnOpenGL.b_lighting.initWindow0
import learnOpenGL.b_lighting.processFrame
import learnOpenGL.common.glEnableVertexAttribArray
import learnOpenGL.common.glVertexAttribPointer
import learnOpenGL.common.loadTexture
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL30.*
import uno.buffer.destroyBuf
import uno.buffer.intBufferBig
import uno.glsl.Program
import java.nio.ByteBuffer
import java.nio.IntBuffer

fun main() {
    with(Framebuffers()) {
        run()
        end()
    }
}

private class Framebuffers {
    val window = initWindow0("Framebuffers")

    val programRender = ProgramRender()
    val programSplash = ProgramSplash()

    enum class Object { Cube, Plane, Quad }

    val vao = intBufferBig<Object>()
    val vbo = intBufferBig<Object>()

    enum class Tex { Cube, Floor, ColorBuffer }

    val tex = intBufferBig<Tex>()

    var rbo = intBufferBig(1)
    val framebuffer: IntBuffer = intBufferBig(1)

    /** vertex attributes for a quad that fills the entire screen in Normalized Device Coordinates. */
    val quadVertices = floatArrayOf(
        // positions    | texCoords
        -1f, +1f, 0f, 1f,
        -1f, -1f, 0f, 0f,
        +1f, -1f, 1f, 0f,

        -1f, +1f, 0f, 1f,
        +1f, -1f, 1f, 0f,
        +1f, +1f, 1f, 1f
    )

    open inner class ProgramRender : Program("shaders/d/_5_1", "framebuffers.vert", "framebuffers.frag") {

        val model = glGetUniformLocation(name, "model")
        val view = glGetUniformLocation(name, "view")
        val proj = glGetUniformLocation(name, "projection")

        init {
            usingProgram(name) { "texture1".unit = semantic.sampler.DIFFUSE }
        }
    }

    open inner class ProgramSplash : Program("shaders/d/_5_1", "framebuffers-screen.vert", "framebuffers-screen.frag") {
        init {
            usingProgram(this.name) { "screenTexture".unit = semantic.sampler.DIFFUSE }
        }
    }

    init {
        glEnable(GL_DEPTH_TEST)
        glGenVertexArrays(vao)
        glGenBuffers(vbo)

        for (i in Object.values()) {
            glBindVertexArray(vao[i])
            glBindBuffer(GL_ARRAY_BUFFER, vbo[i])
            if (i == Object.Quad) {
                glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW)
                glEnableVertexAttribArray(glf.pos2_tc2)
                glVertexAttribPointer(glf.pos2_tc2)
            } else {
                glBufferData(GL_ARRAY_BUFFER, verticesCube2[i.ordinal], GL_STATIC_DRAW)
                glEnableVertexAttribArray(glf.pos3_tc2)
                glVertexAttribPointer(glf.pos3_tc2)
            }
            glBindVertexArray()
        }
        // load textures
        tex[Tex.Cube.ordinal] = loadTexture("textures/marble.jpg")
        tex[Tex.Floor.ordinal] = loadTexture("textures/metal.png")

        // framebuffer configuration
        // -------------------------
        glGenFramebuffers(framebuffer)
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer[0])
        // create a color attachment texture
        tex[Tex.ColorBuffer.ordinal] = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, tex[Tex.ColorBuffer])
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RGB8,
            windowSize.x,
            windowSize.y,
            0,
            GL_RGB,
            GL_UNSIGNED_BYTE,
            null as ByteBuffer?
        )

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, tex[Tex.ColorBuffer], 0)
        // create a renderbuffer object for depth and stencil attachment (we won't be sampling these)
        glGenRenderbuffers(rbo)
        glBindRenderbuffer(GL_RENDERBUFFER, rbo)
        glRenderbufferStorage(
            GL_RENDERBUFFER,
            GL_DEPTH24_STENCIL8,
            windowSize
        ) // use a single renderbuffer object for both a depth AND stencil buffer.
        glFramebufferRenderbuffer(
            GL_FRAMEBUFFER,
            GL_DEPTH_STENCIL_ATTACHMENT,
            GL_RENDERBUFFER,
            rbo
        ) // now actually attach it
        // now that we actually created the framebuffer and added all attachments we want to check if it is actually complete now
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            System.err.println("ERROR::FRAMEBUFFER:: Framebuffer is not complete!")
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        // draw as wireframe
//        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
    }


    fun run() {
        while (window.open) {
            window.processFrame()

            // render
            // bind to framebuffer and draw scene as we normally would to color texture
            glBindFramebuffer(GL_FRAMEBUFFER, framebuffer[0])
            glEnable(GL_DEPTH_TEST) // enable depth testing (is disabled for rendering screen-space quad)

            // make sure we clear the framebuffer's content
            glClearColor(clearColor0)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            glUseProgram(programRender.name)
            var model = Mat4()
            val view = camera.viewMatrix
            val projection = glm.perspective(camera.zoom.rad, window.aspect, 0.1f, 100.0f)
            glUniform(programRender.view, view)
            glUniform(programRender.proj, projection)
            // cubes
            glBindVertexArray(vao[Object.Cube])
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE)
            glBindTexture(GL_TEXTURE_2D, tex[Tex.Cube])
            model.translate_(-1f, 0f, -1f)
            glUniform(programRender.model, model)
            glDrawArrays(GL_TRIANGLES, 36)
            model = Mat4().translate_(2f, 0f, 0f)
            glUniform(programRender.model, model)
            glDrawArrays(GL_TRIANGLES, 36)
            // floor
            glBindVertexArray(vao[Object.Plane])
            glBindTexture(GL_TEXTURE_2D, tex[Tex.Floor])
            glUniform(programRender.model, Mat4())
            glDrawArrays(GL_TRIANGLES, 6)
            glBindVertexArray(0)

            // now bind back to default framebuffer and draw a quad plane with the attached framebuffer color texture
            glBindFramebuffer(GL_FRAMEBUFFER, 0)
            glDisable(GL_DEPTH_TEST) // disable depth test so screen-space quad isn't discarded due to depth test.
            // clear all relevant buffers
            glClearColor(1f, 1f, 1f, 1f)
            glClear(GL_COLOR_BUFFER_BIT)

            glUseProgram(programSplash.name)
            glBindVertexArray(vao[Object.Quad])
            // use the color attachment texture as the texture of the quad plane
            glBindTexture(GL_TEXTURE_2D, tex[Tex.ColorBuffer])
            glDrawArrays(GL_TRIANGLES, 6)

            window.swapAndPoll()
        }
    }

    fun end() {
        glDeleteProgram(programRender.name)
        glDeleteProgram(programSplash.name)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        glDeleteTextures(tex)
        destroyBuf(vao, vbo, tex)
        window.end()
    }

}