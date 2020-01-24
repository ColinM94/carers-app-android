package com.colinmaher.carersapp.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.colinmaher.carersapp.MainActivity
import com.colinmaher.carersapp.R
import com.colinmaher.carersapp.VisitActivity
import com.colinmaher.carersapp.extensions.log
import com.colinmaher.carersapp.models.Client
import com.colinmaher.carersapp.models.Visit
import com.colinmaher.carersapp.models.Visits
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_visits.*
import kotlinx.android.synthetic.main.visit_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.text.SimpleDateFormat
import kotlin.math.log

class VisitsFragment(private var currentUser: FirebaseUser, private var db: FirebaseFirestore) : Fragment() {

    private lateinit var adapter: VisitAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_visits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadData()
    }

    private fun populateList(visits: MutableList<Visit>) {
        adapter = VisitAdapter(this.context!!)
        adapter.replaceItems(visits)
        recyclerview_visit.adapter = adapter

        // Adds divider between items.
        recyclerview_visit.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    private fun loadData() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main){(activity as MainActivity).showSpinner()}

            val visits = mutableListOf<Visit>()

            try{
                // Get current users list of visit Ids.
                var docs = db.collection("visits").whereEqualTo("userId", currentUser.uid).get().await()

                docs.forEach{ doc ->
                    val visit = doc.toObject(Visit::class.java)
                    visit.id = doc.id
                    visits.add(visit)
                }

            }catch(e: Exception){
                log("error")
                log(e.message.toString())
            }

            withContext(Dispatchers.Main){
                if (visits.isNotEmpty()) {
                    populateList(visits)
                }

                (activity as MainActivity).hideSpinner()
            }
        }
    }

    inner class VisitAdapter(private var context: Context) : RecyclerView.Adapter<VisitAdapter.ViewHolder>() {
        private var items = listOf<Visit>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.visit_item, parent, false)
            return ViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]

            val pattern = "MM/dd/yyyy | HH:SS"
            val simpleDateFormat = SimpleDateFormat(pattern)

            holder.containerView.textview_visititem_name.text = item.clientId
            holder.containerView.textview_visititem_location.text = item.startDate + " : " + item.startTime + " - " + item.endTime


            holder.containerView.setOnClickListener {
                val intent = Intent(context, VisitActivity::class.java)
                intent.putExtra("visitId", item.id)

                context.startActivity(intent)
            }
        }

        fun replaceItems(items: List<Visit>) {
            this.items = items
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
            LayoutContainer

        private fun toast(msg: String) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }
}


