package learnOpenGL.a_gettingStarted

import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import gln.buffer.glBindBuffer
import gln.draw.glDrawArrays
import gln.get
import gln.glClearColor
import gln.glf.semantic
import gln.program.usingProgram
import gln.uniform.glUniform
import gln.vertexArray.glBindVertexArray
import gln.vertexArray.glVertexAttribPointer
import learnOpenGL.common.flipY
import learnOpenGL.common.readImage
import learnOpenGL.common.toBuffer
import org.lwjgl.opengl.EXTABGR
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_BGR
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import uno.buffer.destroyBuf
import uno.buffer.floatBufferBig
import uno.buffer.intBufferBig
import uno.glfw.glfw
import uno.glsl.Program

fun main() {
    with(CoordinateSystemsDepth()) {
        run()
        end()
    }
}

val verticesCube = floatArrayOf(
    -0.5f, -0.5f, -0.5f, 0f, 0f,
    +0.5f, -0.5f, -0.5f, 1f, 0f,
    +0.5f, +0.5f, -0.5f, 1f, 1f,
    +0.5f, +0.5f, -0.5f, 1f, 1f,
    -0.5f, +0.5f, -0.5f, 0f, 1f,
    -0.5f, -0.5f, -0.5f, 0f, 0f,

    -0.5f, -0.5f, +0.5f, 0f, 0f,
    +0.5f, -0.5f, +0.5f, 1f, 0f,
    +0.5f, +0.5f, +0.5f, 1f, 1f,
    +0.5f, +0.5f, +0.5f, 1f, 1f,
    -0.5f, +0.5f, +0.5f, 0f, 1f,
    -0.5f, -0.5f, +0.5f, 0f, 0f,

    -0.5f, +0.5f, +0.5f, 1f, 0f,
    -0.5f, +0.5f, -0.5f, 1f, 1f,
    -0.5f, -0.5f, -0.5f, 0f, 1f,
    -0.5f, -0.5f, -0.5f, 0f, 1f,
    -0.5f, -0.5f, +0.5f, 0f, 0f,
    -0.5f, +0.5f, +0.5f, 1f, 0f,

    +0.5f, +0.5f, +0.5f, 1f, 0f,
    +0.5f, +0.5f, -0.5f, 1f, 1f,
    +0.5f, -0.5f, -0.5f, 0f, 1f,
    +0.5f, -0.5f, -0.5f, 0f, 1f,
    +0.5f, -0.5f, +0.5f, 0f, 0f,
    +0.5f, +0.5f, +0.5f, 1f, 0f,

    -0.5f, -0.5f, -0.5f, 0f, 1f,
    +0.5f, -0.5f, -0.5f, 1f, 1f,
    +0.5f, -0.5f, +0.5f, 1f, 0f,
    +0.5f, -0.5f, +0.5f, 1f, 0f,
    -0.5f, -0.5f, +0.5f, 0f, 0f,
    -0.5f, -0.5f, -0.5f, 0f, 1f,

    -0.5f, +0.5f, -0.5f, 0f, 1f,
    +0.5f, +0.5f, -0.5f, 1f, 1f,
    +0.5f, +0.5f, +0.5f, 1f, 0f,
    +0.5f, +0.5f, +0.5f, 1f, 0f,
    -0.5f, +0.5f, +0.5f, 0f, 0f,
    -0.5f, +0.5f, -0.5f, 0f, 1f
)

private class CoordinateSystemsDepth {

    val window = initWindow("Coordinate Systems Depth")

    val program = ProgramA()

    val vbo = intBufferBig(1)
    val vao = intBufferBig(1)

    enum class Texture { A, B }

    val textures = intBufferBig<Texture>()

    val matBuffer = floatBufferBig(16)

    inner class ProgramA : Program("shaders/a/_6_2", "coordinate-systems.vert", "coordinate-systems.frag") {

        val model = glGetUniformLocation(name, "model")
        val view = glGetUniformLocation(name, "view")
        val proj = glGetUniformLocation(name, "projection")

        init {
            usingProgram(name) {
                "textureA".unitE = Texture.A
                "textureB".unitE = Texture.B
            }
        }
    }

    init {
        // configure global opengl state
        glEnable(GL_DEPTH_TEST)

        //  set up vertex data (and buffer(s)) and configure vertex attributes
        glGenVertexArrays(vao)
        glGenBuffers(vbo)

        //  bind the Vertex Array Object first, then bind and set vertex buffer(s), and then configure vertex attributes(s).
        glBindVertexArray(vao)

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, verticesCube, GL_STATIC_DRAW)

        //  position attribute
        glVertexAttribPointer(semantic.attr.POSITION, Vec3.length, GL_FLOAT, false, Vec3.size + Vec2.size, 0)
        glEnableVertexAttribArray(semantic.attr.POSITION)
        // texture coord attribute
        glVertexAttribPointer(semantic.attr.TEX_COORD, Vec2.length, GL_FLOAT, false, Vec3.size + Vec2.size, Vec3.size)
        glEnableVertexAttribArray(semantic.attr.TEX_COORD)


        // load and create a texture
        glGenTextures(textures)

        //  texture A
        glBindTexture(GL_TEXTURE_2D, textures[Texture.A])
        //  set the texture wrapping parameters to GL_REPEAT (default wrapping method)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        // set texture filtering parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        // load image, create texture and generate mipmaps
        var image = readImage("textures/container.jpg").flipY()
        image.toBuffer().use {
            glTexImage2D(GL_RGB, image.width, image.height, GL_BGR, GL_UNSIGNED_BYTE, it)
            glGenerateMipmap(GL_TEXTURE_2D)
        }


        //  texture B
        glBindTexture(GL_TEXTURE_2D, textures[Texture.B])
        //  set the texture wrapping parameters to GL_REPEAT (default wrapping method)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        // set texture filtering parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        // load image, create texture and generate mipmaps
        image = readImage("textures/awesomeface.png").flipY()
        image.toBuffer().use {
            glTexImage2D(GL_TEXTURE_2D, GL_RGB, image.width, image.height, EXTABGR.GL_ABGR_EXT, GL_UNSIGNED_BYTE, it)
            glGenerateMipmap(GL_TEXTURE_2D)
        }

        /*  You can unbind the VAO afterwards so other VAO calls won't accidentally modify this VAO, but this rarely happens.
            Modifying other VAOs requires a call to glBindVertexArray anyways so we generally don't unbind VAOs (nor VBOs)
            when it's not directly necessary.   */
        //glBindVertexArray()
    }

    fun run() {

        while (window.open) {

            window.processInput()

            //  render
            glClearColor(clearColor)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // also clear the depth buffer now!

            //  bind textures on corresponding texture units
            glActiveTexture(GL_TEXTURE0 + Texture.A.ordinal)
            glBindTexture(GL_TEXTURE_2D, textures[Texture.A])
            glActiveTexture(GL_TEXTURE0 + Texture.B.ordinal)
            glBindTexture(GL_TEXTURE_2D, textures[Texture.B])

            usingProgram(program.name) {

                //  create transformations
                val model = glm.rotate(Mat4(), glfw.time.toFloat(), 0.5f, 1f, 0f)
                val view = glm.translate(Mat4(), 0f, 0f, -3f)
                val projection = glm.perspective(45f.rad, window.aspect, 0.1f, 100f)

                //  pass them to the shaders (3 different ways)
                glUniformMatrix4fv(program.model, false, model to matBuffer)
                glUniform(program.view, view)
                /*  note: currently we set the projection matrix each frame, but since the projection matrix rarely
                    changes it's often best practice to set it outside the main loop only once. Best place is the
                    framebuffer size callback   */
                projection to program.proj

                //  render container
                glBindVertexArray(vao)
                glDrawArrays(GL_TRIANGLES, 36)
            }

            window.swapAndPoll()
        }
    }

    fun end() {
        glDeleteProgram(program.name)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        glDeleteTextures(textures)
        destroyBuf(vao, vbo, textures, matBuffer)
        window.end()
    }

}