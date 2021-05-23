package com.example.gl11.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gl11.MainActivity
import com.example.gl11.R
import com.example.gl11.db.FavoriteDB
import com.example.gl11.db.StationDB
import com.example.gl11.entity.DataType
import com.example.gl11.entity.Favorite
import com.example.gl11.entity.Position
import com.example.gl11.gl.PositionUnit
import com.example.gl11.gl.GLRenderer
import com.example.gl11.gl.GLView
import com.example.gl11.ui.home.view.ContentsAdapter
import com.example.gl11.ui.home.view.ContentsDto
import com.example.gl11.ui.home.view.FilterAdapter
import com.example.gl11.ui.home.view.FilterDto
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.controls.Audio
import java.util.*


class HomeFragment : Fragment(),
    GLRenderer.OnSelectUnitListener,
    FilterAdapter.OnSelectListener{

    private var glView: GLView? = null
    private var camera: CameraView? = null
    private var environmentTimer: Timer? = null
    private var filterList: RecyclerView? = null
    private var contentsList: RecyclerView? = null

    private val stationDB: StationDB by lazy { StationDB(requireContext()) }
    private val favoriteDB: FavoriteDB by lazy { FavoriteDB(requireContext()) }

    private val filterDataList = mutableListOf(
        FilterDto(DataType.STATION),
        FilterDto(DataType.FAVORITE),
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val act = (activity as MainActivity)
        glView = GLView(requireContext(), act.rotationSensor, this)
        camera = CameraView(requireContext()).also {
            it.setLifecycleOwner(this.viewLifecycleOwner)
            it.audio = Audio.OFF
        }

        return inflater.inflate(R.layout.fragment_home, container, false).apply{
            filterList = findViewById<RecyclerView>(R.id.filter_list).also{ filter->
                filter.layoutManager = LinearLayoutManager(requireContext()).also {
                    it.orientation = LinearLayoutManager.HORIZONTAL
                }
                filter.setHasFixedSize(true)
                filter.adapter = FilterAdapter(requireContext(), filterDataList).also{
                    it.onSelectListener = this@HomeFragment
                }
            }
            contentsList = findViewById<RecyclerView>(R.id.contents_list).also{ contents->
                contents.visibility = View.GONE
                contents.layoutManager = LinearLayoutManager(requireContext())
            }
            findViewById<FrameLayout>(R.id.root).let { root->
                root.addView(camera, ViewGroup.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT))
                root.addView(glView, ViewGroup.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT))
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                updateList()
            }
        }
        environmentTimer = Timer(false).also {
            it.schedule(task, 1000, 3000)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        environmentTimer?.cancel()
        glView = null
        camera = null
    }

    override fun onSelect(unit: PositionUnit?) {
        updateContents(unit)
    }

    override fun onLikeSelect(unit: PositionUnit) {
    }

    override fun onFavoriteSelect(unit: PositionUnit) {
        if(!unit.isFav){
            val f = favoriteDB.insert(unit)
            (activity as MainActivity).unitList.add(PositionUnit.create(f))
            unit.isFav = true
        }
        updateList()

    }

    override fun onDetailSelect(unit: PositionUnit) {

    }

    override fun onSelect(filter: FilterDto) {
        filterDataList
            .filter { it.type == filter.type && it.user == filter.user }
            .forEach {
                 it.isSelected = !it.isSelected
        }
        filterList?.adapter?.notifyDataSetChanged()
        updateList()
    }

    private fun updateList(){
        val act = (activity as MainActivity)
        if(act.isReady){
            val filter = filterDataList.filter { it.isSelected }
            val all = (filter.isEmpty() || filter.size == filterDataList.size)
            val s = all || filter.any { it.type == DataType.STATION }
            val f = all || filter.any { it.type == DataType.FAVORITE }
            val list = mutableListOf<PositionUnit>()
            if(all){
                val stationFav = act.unitList.filter { it.type == DataType.STATION && it.isFav }
                list.addAll(act.unitList
                    .filter { !((it.type == DataType.FAVORITE) && stationFav.map { sf->sf.id }.contains(it.id)) })
            } else{
                if(s){
                    list.addAll(act.unitList.filter { it.type == DataType.STATION })
                }
                if(f){
                    list.addAll(act.unitList.filter { it.type == DataType.FAVORITE })
                }
            }
            glView?.updateList(list)
        }
        act.runOnUiThread{
            (contentsList?.adapter as? ContentsAdapter)?.let{ a->
                act.unitList.filter { a.top.type == it.type && a.top.id == it.id }.forEach {
                    a.top.distance = it.distanceText
                }
                a.notifyDataSetChanged()
            }
        }
    }
    private fun updateContents(unit:PositionUnit?){
        val listView = contentsList!!
        unit?.let{ u->
            when(u.type){
                DataType.STATION ->{
                    stationDB.selectId(u.id).firstOrNull()?.let{ d->
                        listView.adapter = ContentsAdapter(
                            requireContext(),
                            ContentsDto.create(d, u))
                        listView.visibility = View.VISIBLE
                    }
                }
                DataType.FAVORITE ->{
                    favoriteDB.selectId(u.id).firstOrNull()?.let{ d->
                        listView.adapter = ContentsAdapter(
                            requireContext(),
                            ContentsDto.create(d, u))
                        listView.visibility = View.VISIBLE
                    }
                }
                else -> {}
            }
        } ?: run{
            listView.visibility = View.GONE
        }
    }
}