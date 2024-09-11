package com.example.geo_tracker.adaptor;

import static com.example.geo_tracker.UtilityFunctions.convertByteArrayToBitmap;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geo_tracker.R;
import com.example.geo_tracker.UtilityFunctions;
import com.example.geo_tracker.activity.PathActivity;
import com.example.geo_tracker.database.path.Path;

import java.util.List;

/**
 * Adapter for Path objects.
 */
public class PathAdapter extends RecyclerView.Adapter<PathAdapter.PathViewHolder> {
    private List<Path> pathList;

    public PathAdapter(List<Path> pathList) {
        this.pathList = pathList;

    }

    @NonNull
    @Override
    public PathViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.path_recycleview, parent, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = ((RecyclerView) parent).getChildLayoutPosition(v);
                Path item = pathList.get(itemPosition);
                int path_id = item.id;
                Intent i = new Intent(parent.getContext(), PathActivity.class);
                i.putExtra("path_id", path_id);
                parent.getContext().startActivity(i);
            }
        });
        return new PathViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PathViewHolder holder, int position) {
        holder.bind(pathList.get(position));
    }
    public void setData(List<Path> newData) {
        if (pathList != null) {
            pathList.clear();
            pathList.addAll(newData);
            notifyDataSetChanged();
        } else {
            pathList = newData;
        }
    }

    @Override
    public int getItemCount() {
        return pathList.size();
    }

    public static class PathViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView, distanceTextView, durationTextView, speedTextView, dateTextView;
        public PathViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);
            speedTextView = itemView.findViewById(R.id.speedTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);

        }
        void bind(final Path path){
            if(path != null){
                if(path.image != null) {
                    imageView.setImageBitmap(convertByteArrayToBitmap(path.image));
                }
                nameTextView.setText(path.id + ". Path " + path.name);
                distanceTextView.setText("Total Distance: " + UtilityFunctions.formatDistance(path.distance));
                durationTextView.setText("Duration: " + UtilityFunctions.formatTime(path.time));
                dateTextView.setText("Date: "+path.date);
                double averageSpeed = path.avg_speed;
                String s = String.format("Average speed %.2f m/s", averageSpeed);
                speedTextView.setText(s);

            }

        }
    }


}

