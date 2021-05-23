package com.example.gl11.gl

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.gl11.R
import com.example.gl11.entity.DataType
import com.example.gl11.system.RotationSensor
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLRenderer(val context: Context): GLSurfaceView.Renderer {
    interface OnSelectUnitListener{
        fun onSelect(unit:PositionUnit?)
        fun onLikeSelect(unit:PositionUnit)
        fun onFavoriteSelect(unit:PositionUnit)
        fun onDetailSelect(unit:PositionUnit)
    }
    var scaleRange = 500f
    var rotationSensor: RotationSensor? = null
    var onSelectUnitListener:OnSelectUnitListener? = null

    private enum class ICType(val rid: Int){
        INVISIBLE(R.drawable.ic_w_invisible),
        VISIBLE(R.drawable.ic_w_visible),
        LIKE_ON(R.drawable.ic_r_like),
        LIKE_OFF(R.drawable.ic_w_like),
        FAV_ON(R.drawable.ic_y_fav),
        FAV_OFF(R.drawable.ic_w_fav)
    }
    private val lock = Any()
    private val positionList = mutableListOf<PositionUnit>()

    private val util = GLUtil()
    private val wireSphere = WireSphere(40, 20)
    private val rect = TexRectangular(RECT_SIZE, RECT_SIZE)
    private val tex: Texture = Texture()
    private val icTexMap = mapOf(
        ICType.INVISIBLE to Texture(),
        ICType.VISIBLE to Texture(),
        ICType.LIKE_ON to Texture(),
        ICType.LIKE_OFF to Texture(),
        ICType.FAV_ON to Texture(),
        ICType.FAV_OFF to Texture(),
    )

    private var validProgram = false
    private var aspect = 0f
    private var viewWidth = 0f
    private var viewHeight = 0f

    override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
        validProgram = util.makeProgram()
        GLES20.glEnableVertexAttribArray(util.positionHandle)
        GLES20.glEnableVertexAttribArray(util.normalHandle)
        GLES20.glEnableVertexAttribArray(util.texCoordinateHandle)

        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glFrontFace(GLES20.GL_CCW)
        GLES20.glCullFace(GLES20.GL_BACK)

        GLES20.glUniform4f(util.lightAmbientHandle, 1.5f, 1.5f, 1.5f, 1.0f)
        GLES20.glUniform4f(util.lightDiffuseHandle, 1.75f, 1.75f, 1.75f, 1.0f)
        GLES20.glUniform4f(util.lightSpecularHandle, 2.0f, 2.0f, 2.0f, 1.0f)

        GLES20.glEnable(GLES20.GL_TEXTURE_2D)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA) // 単純なアルファブレンド

        icTexMap.forEach {
            it.value.makeTexture(BitmapFactory.decodeResource(
                context.resources,
                it.key.rid,
                BitmapFactory.Options().apply {
                    inScaled = false
                })
            )
        }
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        viewWidth = width.toFloat()
        viewHeight = height.toFloat()
        aspect = viewWidth / viewHeight
    }

    override fun onDrawFrame(unused: GL10?) {
        if (!validProgram) return
        GLES20.glVertexAttribPointer(util.positionHandle, 3, GLES20.GL_FLOAT, false, 0, DummyBuffer)
        GLES20.glVertexAttribPointer(util.normalHandle, 3, GLES20.GL_FLOAT, false, 0,DummyBuffer)
        GLES20.glVertexAttribPointer(util.texCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0,DummyBuffer)
        util.disableTexture()
        util.enableShading()

        GLES20.glClearColor(0f, 0f, 0f, 0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        val pMatrix = FloatArray(16)
        util.gluPerspective(pMatrix, 45f, aspect,.01f,120.0f)
        util.setPMatrix(pMatrix)

        val cMatrix = FloatArray(16)
        val c1Matrix = FloatArray(16)
        Matrix.setLookAtM(c1Matrix, 0,
            0f, 0f, 0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 1.0f, 0.0f)
        rotationSensor?.matrix?.let{c2Matrix->
            Matrix.multiplyMM(cMatrix, 0, c1Matrix, 0, c2Matrix, 0) //cMatrix = c1Matrix * c2Matrix
            util.setCMatrix(cMatrix)
        } ?: util.setCMatrix(c1Matrix)

        util.setLightPosition(floatArrayOf(0f, 1.5f, 3f, 1f))

        val mMatrix = FloatArray(16)
        util.disableShading()
        Matrix.setIdentityM(mMatrix, 0)
        Matrix.scaleM(mMatrix, 0, 100f, 100f, 100f)
        Matrix.rotateM(mMatrix, 0, 90f, 1f, 0f, 0f)
        util.updateMatrix(mMatrix)
        wireSphere.draw(util, .5f, 0f, 0f, 1f)
        util.enableShading()

        util.disableShading()
        util.enableTexture()

        val list = mutableListOf<PositionUnit>()

        getPositionList(list)
        list
            .sortedBy { it.drawDistance ?: Float.MAX_VALUE }
            .take(DISP_UNIT_MAX)
            .sortedBy { it.drawDistance?.let{d->-1*d} ?: Float.MAX_VALUE }
            .forEach { position->
                drawTex(position)
            }
        util.disableTexture()
    }

    fun onTouchEvent(x:Float, y:Float){
        synchronized(lock){
            val prevUnit = positionList.firstOrNull{it.isSelected}
            val posX = if(x == 0f){ -1f } else {x / viewWidth * 2f} - 1f
            val posY = if(y == 0f){ 1f } else { -(y / viewHeight * 2f - 1f)}
            val targetList = positionList
                .sortedBy { it.drawDistance ?: Float.MAX_VALUE }
                .take(DISP_UNIT_MAX)

            val selectUnit = hitUnit(posX, posY, targetList)
            val arrowUnit = hitUnitArrow(posX, posY, targetList)
            val likeUnit = hitUnitLike(posX, posY, targetList)
            val favUnit = hitUnitFav(posX, posY, targetList)
            if(prevUnit != null && selectUnit != null){
                when {
                    arrowUnit != null -> {
                        arrowUnit.let { it.isVisible = !it.isVisible }
                    }
                    likeUnit != null -> {
                        likeUnit.let{
                            onSelectUnitListener?.onLikeSelect(it)
                        }
                    }
                    favUnit != null -> {
                        favUnit.let{
                            onSelectUnitListener?.onFavoriteSelect(it)
                        }
                    }
                    prevUnit == selectUnit -> {
                        onSelectUnitListener?.onDetailSelect(selectUnit)
                    }
                }
            }
            if(prevUnit != selectUnit){
                onSelectUnitListener?.onSelect(selectUnit)
            }
            positionList.forEach {p-> p.isSelected = false }
            selectUnit?.isSelected = true
        }
    }

    fun setPositionList(list:List<PositionUnit>){
        synchronized(lock){
            positionList.clear()
            positionList.addAll(list)
        }
    }

    private fun getPositionList(out:MutableList<PositionUnit>){
        synchronized(lock){
            out.clear()
            out.addAll(positionList)
        }
    }

    private fun drawTex(unit:PositionUnit){
        tex.makeTexture(unit)

        val mMatrix = FloatArray(16)
        Matrix.setIdentityM(mMatrix, 0)
        Matrix.rotateM(mMatrix, 0, 90f, 1f, 0f, 0f)
        unit.drawAngle?.let{
            Matrix.rotateM(mMatrix, 0, it, 0f, 1f, 0f)
        }
        if(unit.isVisible){
            Matrix.rotateM(mMatrix, 0, -90f, 1f, 0f, 0f)
        } else{
            unit.drawDistance?.let{
                if(it < 1000){
                    val r = 0.09f * (1000 - it)
                    Matrix.rotateM(mMatrix, 0, r, 1f, 0f, 0f)
                }
            }
        }
        unit.drawDistance?.let{
            Matrix.translateM(mMatrix, 0, 0f, 0f, it / scaleRange)
        }
        Matrix.rotateM(mMatrix, 0, 180f, 0f, 1f, 0f)

        util.updateMatrix(mMatrix) //現在の変換行列をシェーダに指定
        tex.setTexture(util)
        rect.draw(util, 1f, 1f, 1f, 0.5f, 20f)
        saveRect(unit)

        // distance
        unit.drawDistance?.let{
            if(it < 1000){
                drawDistance(mMatrix, unit)
            }
        }
        // icon
        if(unit.isSelected){
            drawArrowIcon(mMatrix, unit)
            if(unit.type != DataType.FAVORITE) {
                drawFavIcon(mMatrix, unit)
            }
            if(unit.type == DataType.USER){
                drawLikeIcon(mMatrix, unit)
            }
        }
    }
    private fun drawDistance(matrix:FloatArray, unit:PositionUnit){
        val mMatrix = matrix.copyOf()
        Matrix.translateM(mMatrix, 0, 0f, -0.2f, 0.1f)
        Matrix.scaleM(mMatrix, 0, 0.2f, 0.2f, 0.2f)
        util.updateMatrix(mMatrix)
        tex.makeTexture(unit.distanceText, false, unit.colors, Color.TRANSPARENT)
        tex.setTexture(util)
        rect.draw(util,1f, 1f, 1f, 0.5f, 20f)
    }

    private fun drawArrowIcon(matrix:FloatArray, unit:PositionUnit){
        val mMatrix = matrix.copyOf()
        Matrix.translateM(mMatrix, 0, 0.15f, 0.15f, 0.1f)
        Matrix.scaleM(mMatrix, 0, 0.2f, 0.2f, 0.2f)
        util.updateMatrix(mMatrix)
        if(unit.isVisible){
            icTexMap[ICType.VISIBLE]?.setTexture(util)
        } else {
            icTexMap[ICType.INVISIBLE]?.setTexture(util)
        }
        rect.draw(util,1f, 1f, 1f, 0.5f, 20f)
        saveRect(unit.arrow)
    }

    private fun drawLikeIcon(matrix:FloatArray, unit:PositionUnit){
        val mMatrix = matrix.copyOf()
        Matrix.translateM(mMatrix, 0, 0f, 0.15f, 0.1f)
        Matrix.scaleM(mMatrix, 0, 0.2f, 0.2f, 0.2f)
        util.updateMatrix(mMatrix)
        if(unit.isLike){
            icTexMap[ICType.LIKE_ON]?.setTexture(util)
        } else {
            icTexMap[ICType.LIKE_OFF]?.setTexture(util)
        }
        rect.draw(util,1f, 1f, 1f, 0.5f, 20f)
        saveRect(unit.fav)
    }

    private fun drawFavIcon(matrix:FloatArray, unit:PositionUnit){
        val mMatrix = matrix.copyOf()
        Matrix.translateM(mMatrix, 0, -0.15f, 0.15f, 0.1f)
        Matrix.scaleM(mMatrix, 0, 0.2f, 0.2f, 0.2f)
        util.updateMatrix(mMatrix)
        if(unit.isFav){
            icTexMap[ICType.FAV_ON]?.setTexture(util)
        } else {
            icTexMap[ICType.FAV_OFF]?.setTexture(util)
        }
        rect.draw(util,1f, 1f, 1f, 0.5f, 20f)
        saveRect(unit.fav)
    }

    private fun saveRect(unit:DisplayUnit){
        val r = RECT_SIZE / 2
        util.transformPCM(unit.tl, floatArrayOf(-r, r, 0f, 1f))
        util.transformPCM(unit.tr, floatArrayOf(r, r, 0f, 1f))
        util.transformPCM(unit.bl, floatArrayOf(-r, -r, 0f, 1f))
        util.transformPCM(unit.br, floatArrayOf(r, -r, 0f, 1f))
    }
    private fun hitUnit(posX:Float, posY:Float, targetList:List<PositionUnit>):PositionUnit?{
        return targetList.firstOrNull { it.isHit(posX, posY) }
    }

    private fun hitUnitArrow(posX:Float, posY:Float, targetList:List<PositionUnit>):PositionUnit?{
        return targetList.firstOrNull { it.isSelected && it.arrow.isHit(posX, posY) }
    }
    private fun hitUnitLike(posX:Float, posY:Float, targetList:List<PositionUnit>):PositionUnit?{
        return targetList.firstOrNull { it.isSelected && it.type == DataType.USER && it.like.isHit(posX, posY) }
    }
    private fun hitUnitFav(posX:Float, posY:Float, targetList:List<PositionUnit>):PositionUnit?{
        return targetList.firstOrNull { it.isSelected && it.type != DataType.FAVORITE && it.fav.isHit(posX, posY) }
    }

    companion object {
        private val DummyFloat = FloatArray(1)
        private val DummyBuffer = BufferUtil.makeFloatBuffer(DummyFloat)
        private const val RECT_SIZE = 0.5f
        private const val DISP_UNIT_MAX = 50
    }
}
