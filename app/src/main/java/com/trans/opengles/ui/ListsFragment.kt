package com.trans.opengles.ui

import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SimpleOnItemTouchListener
import com.trans.opengles.R
import com.trans.opengles.meta.basic.ARMeta
import com.trans.opengles.meta.basic.BackgroundMeta
import com.trans.opengles.meta.basic.CircleMeta
import com.trans.opengles.meta.basic.ConeMeta
import com.trans.opengles.meta.basic.CylinderMeta
import com.trans.opengles.meta.basic.LinesES30Meta
import com.trans.opengles.meta.basic.PointsES30Meta
import com.trans.opengles.meta.basic.BallMeta
import com.trans.opengles.meta.basic.GlobeMeta
import com.trans.opengles.meta.basic.SquareES30Meta
import com.trans.opengles.meta.basic.SquareMeta
import com.trans.opengles.meta.basic.Texture2DMeta
import com.trans.opengles.meta.basic.TriangleES30Meta
import com.trans.opengles.meta.basic.TriangleMeta
import com.trans.opengles.ui.act.OpenGLActivity
import com.trans.opengles.ui.adap.CommonAdapter
import com.trans.opengles.ui.bean.SampleItem

/**
 * @author Tom灿
 * @description:
 * @date :2023/9/13 9:28
 */
class ListsFragment : Fragment() {

    private lateinit var samples: ArrayList<SampleItem>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        samples = ArrayList(
            listOf(
                SampleItem(
                    getString(R.string.cover_background),
                    getString(R.string.cover_background_des),
                    BackgroundMeta::class.java
                ),
                SampleItem(
                    getString(R.string.cover_points),
                    getString(R.string.cover_points_des),
                    PointsES30Meta::class.java
                ),
                SampleItem(
                    getString(R.string.cover_lines),
                    getString(R.string.cover_lines_des),
                    LinesES30Meta::class.java
                ),
                SampleItem(
                    getString(R.string.cover_triangle),
                    getString(R.string.cover_triangle_des),
                    TriangleMeta::class.java
                ),
                SampleItem(
                    getString(R.string.cover_trianglees30),
                    getString(R.string.cover_trianglees30_des),
                    TriangleES30Meta::class.java
                ),
                SampleItem(
                    getString(R.string.cover_square),
                    getString(R.string.cover_square_des),
                    SquareMeta::class.java
                ),
                SampleItem(
                    getString(R.string.cover_squarees30),
                    getString(R.string.cover_squarees30_des),
                    SquareES30Meta::class.java
                ),
                SampleItem(
                    getString(R.string.cover_circle),
                    getString(R.string.cover_circle_des),
                    CircleMeta::class.java
                ),
                SampleItem(
                    getString(R.string.cover_texture2D),
                    getString(R.string.cover_texture2D_des),
                    Texture2DMeta::class.java
                ),
                SampleItem(
                    getString(R.string.cover_cone),
                    getString(R.string.cover_cone_des),
                    ConeMeta::class.java
                ),
                SampleItem(
                    getString(R.string.cover_cylinder),
                    getString(R.string.cover_cylinder_des),
                    CylinderMeta::class.java
                ),
                SampleItem(
                    getString(R.string.cover_ball),
                    getString(R.string.cover_ball_des),
                    BallMeta::class.java
                ),
                SampleItem(
                    getString(R.string.cover_globe),
                    getString(R.string.cover_globe_des),
                    GlobeMeta::class.java
                ),
                SampleItem(
                    getString(R.string.cover_ar),
                    getString(R.string.cover_ar_des),
                    ARMeta::class.java
                )
            ),
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_list, container, false)
        var recyclerView = root.findViewById<RecyclerView>(R.id.map_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addOnItemTouchListener(SimpleOnItemTouchListener())
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = CommonAdapter(samples).apply {
            setOnItemClickListener(object : CommonAdapter.OnItemClickListener {
                override fun itemClick(sampleItem: SampleItem) {
                    startActivity(Intent(activity, OpenGLActivity::class.java).apply {
                        putExtra("class", Bundle().apply {
                            putSerializable("class", sampleItem.clazz)
                        })
                    })
                }
            })
        }
        return root
    }


}