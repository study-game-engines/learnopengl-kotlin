package learnOpenGL.a_gettingStarted

import glm_.vec3.Vec3
import gln.buffer.glBindBuffer
import gln.draw.glDrawArrays
import gln.glClearColor
import gln.glf.semantic
import gln.vertexArray.glBindVertexArray
import gln.vertexArray.glVertexAttribPointer
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.glDeleteVertexArrays
import org.lwjgl.opengl.GL30.glGenVertexArrays
import uno.buffer.destroyBuf
import uno.buffer.intBufferBig
import uno.glsl.Program

fun main() {
    with(ShadersClass()) {
        run()
        end()
    }
}

private class ShadersClass {
    val window = initWindow("Shaders Class")

    // build and compile our shader program, we can simply use it as int for the moment
    val program = ProgramA()

    val vbo = intBufferBig(1)
    val vao = intBufferBig(1)

    val vertices = floatArrayOf(
        // positions | colors
        +0.5f, -0.5f, 0f, 1f, 0f, 0f, // bottom right
        -0.5f, -0.5f, 0f, 0f, 1f, 0f, // bottom left
        +0.0f, +0.5f, 0f, 0f, 0f, 1f  // top
    )

    class ProgramA : Program("shaders/a/_3_3", "shader.vert", "shader.frag")

    init {

        //  set up vertex data (and buffer(s)) and configure vertex attributes
        glGenVertexArrays(vao)
        glGenBuffers(vbo)
        //  bind the Vertex Array Object first, then bind and set vertex buffer(s), and then configure vertex attributes(s).
        glBindVertexArray(vao)

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        //  position attribute
        glVertexAttribPointer(semantic.attr.POSITION, Vec3.length, GL_FLOAT, false, 2 * Vec3.size, 0)
        glEnableVertexAttribArray(semantic.attr.POSITION)
        //  color attribute
        glVertexAttribPointer(semantic.attr.COLOR, Vec3.length, GL_FLOAT, false, 2 * Vec3.size, Vec3.size)
        glEnableVertexAttribArray(semantic.attr.COLOR)

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
            glClear(GL_COLOR_BUFFER_BIT)

            // render the triangle
            glUseProgram(program.name)
            glBindVertexArray(vao)
            glDrawArrays(GL_TRIANGLES, 3)

            //  glfw: swap buffers and poll IO events (keys pressed/released, mouse moved etc.)
            window.swapAndPoll()
        }
    }

    fun end() {
        //  optional: de-allocate all resources once they've outlived their purpose:
        glDeleteProgram(program.name)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        destroyBuf(vao, vbo)
        window.end()
    }

}
