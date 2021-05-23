package com.example.gl11.gl

import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import kotlin.math.tan

//シェーダ操作
class GLUtil {
    //システム
    //shadingを行うflagのハンドル
    private var enableShadingHandle = 0
    //shadingを行わない時に使う単色ハンドル
    var objectColorHandle = 0
    //Textureを使うflagのハンドル
    private var enableTextureHandle = 0

    //光源のハンドル
    //光源の環境光色ハンドル
    var lightAmbientHandle = 0
    //光源の拡散光色ハンドル
    var lightDiffuseHandle = 0
    //光源の鏡面光色ハンドル
    var lightSpecularHandle = 0
    //光源の位置ハンドル
    private var lightPosHandle = 0

    //マテリアルのハンドル
    //マテリアルの環境光色ハンドル
    var materialAmbientHandle = 0
    //マテリアルの拡散光色ハンドル
    var materialDiffuseHandle = 0
    //マテリアルの鏡面光色ハンドル
    var materialSpecularHandle = 0
    //マテリアルの鏡面指数ハンドル
    var materialShininessHandle = 0

    //行列のハンドル
    //モデルビュー行列ハンドル（カメラビュー行列×モデル変換行列）
    private var mMatrixHandle = 0
    //(射影行列×モデルビュー行列)ハンドル
    private var pmMatrixHandle = 0

    //頂点のハンドル
    //位置ハンドル
    var positionHandle = 0
    //法線ハンドル
    var normalHandle = 0

    //テクスチャのハンドル
    //テクスチャコードハンドル
    var texCoordinateHandle = 0
    //テクスチャハンドル
    var textureHandle = 0

    //行列
    private var cMatrix = FloatArray(16) //視点変換直後のモデルビュー行列
    private var mvMatrix = FloatArray(16) //モデルビュー行列
    private var pMatrix = FloatArray(16) //射影行列
    private var pmMatrix = FloatArray(16) //射影行列 pMatrix*mvMatrix

    //光源
    private val lightPos = FloatArray(4) //光源の座標　x,y,z　（ワールド座標）
    private val cvLightPos = FloatArray(4) //光源の座標　x,y,z　（カメラビュー座標）

    //プログラムの生成
    fun makeProgram(): Boolean {
        //シェーダーオブジェクトの生成
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_CODE)
        if (vertexShader == -1) return false
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_CODE)
        if (fragmentShader == -1) return false

        //プログラムオブジェクトの生成
        val program: Int = GLES20.glCreateProgram() //プログラムオブジェクト
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        // リンクエラーチェック
        val linked = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linked, 0)
        if (linked[0] <= 0) {
            Log.e(" makeProgram", "Failed in Linking")
            Log.e(" makeProgram", GLES20.glGetProgramInfoLog(program))
            return false
        }

        //shading可否ハンドルの取得
        enableShadingHandle = GLES20.glGetUniformLocation(program, "u_EnableShading")
        //texture可否ハンドルの取得
        enableTextureHandle = GLES20.glGetUniformLocation(program, "u_EnableTexture")

        //光源のハンドルの取得
        lightAmbientHandle = GLES20.glGetUniformLocation(program, "u_LightAmbient")
        lightDiffuseHandle = GLES20.glGetUniformLocation(program, "u_LightDiffuse")
        lightSpecularHandle = GLES20.glGetUniformLocation(program, "u_LightSpecular")
        lightPosHandle = GLES20.glGetUniformLocation(program, "u_LightPos")

        //マテリアルのハンドルの取得
        materialAmbientHandle = GLES20.glGetUniformLocation(program, "u_MaterialAmbient")
        materialDiffuseHandle = GLES20.glGetUniformLocation(program, "u_MaterialDiffuse")
        materialSpecularHandle = GLES20.glGetUniformLocation(program, "u_MaterialSpecular")
        materialShininessHandle = GLES20.glGetUniformLocation(program, "u_MaterialShininess")
        //光源を使わない時のマテリアルの色のハンドルの取得
        objectColorHandle = GLES20.glGetUniformLocation(program, "u_ObjectColor")

        //行列のハンドルの取得
        mMatrixHandle = GLES20.glGetUniformLocation(program, "u_MMatrix")
        pmMatrixHandle = GLES20.glGetUniformLocation(program, "u_PMMatrix")

        //頂点とその法線ベクトルのハンドルの取得
        positionHandle = GLES20.glGetAttribLocation(program, "a_Position")
        normalHandle = GLES20.glGetAttribLocation(program, "a_Normal")

        //テクスチャのハンドルの取得
        texCoordinateHandle = GLES20.glGetAttribLocation(program, "a_Texcoord")
        textureHandle = GLES20.glGetUniformLocation(program, "u_Texture")

        GLES20.glUseProgram(program)
        enableShading()
        disableTexture()
        return true
    }

    //シェーダーオブジェクトの生成
    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        // コンパイルチェック
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e(" loadShader", "Failed in Compilation")
            Log.e(" loadShader", GLES20.glGetShaderInfoLog(shader))
            return -1
        }
        return shader
    }

    //透視変換の指定
    fun gluPerspective(pm: FloatArray?, angle: Float, aspect: Float, near: Float, far: Float) {
        val top: Float
        val bottom: Float
        val left: Float
        val right: Float
        if (aspect < 1f) {
            top = near * tan(angle * (Math.PI / 360.0)).toFloat()
            bottom = -top
            left = bottom * aspect
            right = -left
        } else {
            right = 1.1f * near * tan(angle * (Math.PI / 360.0)).toFloat()
            left = -right
            bottom = left / aspect
            top = -bottom
        }
        Matrix.frustumM(pm, 0, left, right, bottom, top, near, far)
    }

    //ワールド座標系のLightPosを受け取る
    fun setLightPosition(lp: FloatArray?) {
        System.arraycopy(lp as Any, 0, lightPos, 0, 4)
    }

    //射影行列をシェーダに指定
    fun setPMatrix(pm: FloatArray?) {
        System.arraycopy(pm as Any, 0, pMatrix, 0, 16)
    }

    //カメラ視点変換行列を登録
    fun setCMatrix(cm: FloatArray?) {
        System.arraycopy(cm as Any, 0, cMatrix, 0, 16)
    }

    //モデルビュー変換行列 ＝ （カメラのビュー変換行列 × モデル変換行列） をシェーダに指定
    fun updateMatrix(mm: FloatArray?) {
        Matrix.multiplyMM(mvMatrix, 0, cMatrix, 0, mm, 0) //mvMatrix = cMatrix * mm
        Matrix.multiplyMM(pmMatrix, 0, pMatrix, 0, mvMatrix, 0) //pmMatrix = pMatrix * mvMatrix
        //モデルビュー変換行列をシェーダに指定
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mvMatrix, 0)

        //プロジェクション行列（射影行列）×モデルビュー行列をシェーダに指定
        GLES20.glUniformMatrix4fv(pmMatrixHandle, 1, false, pmMatrix, 0)

        //シェーダはカメラビュー座標系の光源位置を使う
        //ワールド座標系のLightPosを，カメラビュー座標系に変換してシェーダに送る
        Matrix.multiplyMV(cvLightPos, 0, cMatrix, 0, lightPos, 0)
        GLES20.glUniform4f(lightPosHandle, cvLightPos[0], cvLightPos[1], cvLightPos[2], 1.0f)
    }

    fun transformPCM(result: FloatArray, source: FloatArray?) {
        Matrix.multiplyMV(result, 0, pmMatrix, 0, source, 0)
        result[0] /= result[3]
        result[1] /= result[3]
        result[2] /= result[3]
        //result[3]にはsourceのz要素が符号反転されて入っている
    }

    fun enableShading() {
        GLES20.glUniform1i(enableShadingHandle, 1)
    }

    fun disableShading() {
        GLES20.glUniform1i(enableShadingHandle, 0)
    }

    fun enableTexture() {
        GLES20.glUniform1i(enableTextureHandle, 1)
    }

    fun disableTexture() {
        GLES20.glUniform1i(enableTextureHandle, 0)
    }
    companion object{
        //頂点シェーダのコード
        private const val VERTEX_CODE =  //shadingを使用するflag 1の時使用する，0の時使用しない（単色にする）
            "uniform highp int u_EnableShading;" +  //textureを使用する：1  使用しない：0
                    "uniform highp int u_EnableTexture;" +  //shadingを使用しない時の色の設定（単色）
                    "uniform vec4 u_ObjectColor;" +  //光源
                    "uniform vec4 u_LightAmbient;" +  //光源の環境光色
                    "uniform vec4 u_LightDiffuse;" +  //光源の拡散光色
                    "uniform vec4 u_LightSpecular;" +  //光源の鏡面光色
                    "uniform vec4 u_LightPos;" +  //光源の位置（カメラビュー座標系）
                    //マテリアル
                    "uniform vec4 u_MaterialAmbient;" +  //マテリアルの環境光色
                    "uniform vec4 u_MaterialDiffuse;" +  //マテリアルの拡散光色
                    "uniform vec4 u_MaterialSpecular;" +  //マテリアルの鏡面光色
                    "uniform float u_MaterialShininess;" +  //マテリアルの鏡面指数
                    //行列
                    "uniform mat4 u_MMatrix;" +  //モデルビュー行列
                    "uniform mat4 u_PMMatrix;" +  //射影行列×モデルビュー行列
                    //頂点情報
                    "attribute vec4 a_Position;" +  //位置
                    "attribute vec3 a_Normal;" +  //法線
                    // テクスチャ情報
                    "attribute vec2 a_Texcoord;" +  //テクスチャ
                    //出力
                    "varying vec4 v_Color;" + "" +
                    "varying vec2 v_Texcoord;" +
                    "void main() {" +
                    "if (u_EnableShading==1) {" +  //環境光の計算
                    "vec4 ambient=u_LightAmbient*u_MaterialAmbient;" +  //拡散光の計算
                    "vec3 P=vec3(u_MMatrix*a_Position);" +
                    "vec3 L=normalize(vec3(u_LightPos)-P);" +  //光源方向単位ベクトル
                    "vec3 N=normalize(mat3(u_MMatrix)*a_Normal);" +  //法線単位ベクトル
                    "float dotLN=max(dot(L,N),0.0);" +
                    "vec4 diffuseP=vec4(dotLN);" +
                    "vec4 diffuse=diffuseP*u_LightDiffuse*u_MaterialDiffuse;" +  //鏡面光の計算
                    "vec3 V=normalize(-P);" +  //視点方向単位ベクトル
                    "float dotNLEffect=ceil(dotLN);" +
                    "vec3 R=2.*dotLN*N-L;" +
                    "float specularP=pow(max(dot(R,V),0.0),u_MaterialShininess)*dotNLEffect;" +
                    "vec4 specular=specularP*u_LightSpecular*u_MaterialSpecular;" +  //色の指定
                    "v_Color=ambient+diffuse+specular;" +
                    "} else {" +
                    "v_Color=u_ObjectColor;" +
                    "}" +  //位置の指定
                    "gl_Position=u_PMMatrix*a_Position;" +
                    "if (u_EnableTexture==1) {" +  //テクスチャの指定
                    "v_Texcoord = a_Texcoord;" +
                    "}" +
                    "}"

        //フラグメントシェーダのコード
        private const val FRAGMENT_CODE = "precision mediump float;" +  //textureを使用する：1  使用しない：0
                "uniform highp int u_EnableShading;" +  //textureを使用するflag 1の時使用する，0の時使用しない（単色にする）
                "uniform highp int u_EnableTexture;" +
                "uniform sampler2D u_Texture;" +
                "varying vec2 v_Texcoord;" +
                "varying vec4 v_Color;" +
                "void main() {" +
                "if (u_EnableTexture==1) {" +
                "if (u_EnableShading==1) {" +
                "gl_FragColor = v_Color*texture2D(u_Texture, v_Texcoord);" +
                "} else {" +
                "gl_FragColor = texture2D(u_Texture, v_Texcoord);" +
                "}" +
                "} else {" +
                "gl_FragColor=v_Color;" +
                "}" +
                "}"
    }
}