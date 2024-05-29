package com.example.task101d.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.task101d.R;

import java.text.MessageFormat;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {
    private final boolean[] answerStates;
    private final String[] selectedAnswers;
    private final String[] correctAnswers;
    private final Context context;

    public ResultsAdapter(boolean[] answerStates, String[] selectedAnswers, String[] correctAnswers, Context context) {
        this.answerStates = answerStates;
        this.selectedAnswers = selectedAnswers;
        this.correctAnswers = correctAnswers;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_answer, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvTitle.setText(MessageFormat.format("Question {0}", position+1));
        holder.tvAnswer.setText(selectedAnswers[position]);
        if (answerStates[position]) {
            // If correct, hide the correction views and show "Correct!"
            holder.tvCorrect.setText(R.string.correct);
            holder.tvCorrectAnswer.setVisibility(View.GONE);
            holder.tvCorrectAnswerLabel.setVisibility(View.GONE);
        } else {
            // If false, show the correct answer in the additional views
            holder.tvCorrect.setText(R.string.incorrect);
            holder.tvCorrectAnswer.setText(correctAnswers[position]);
            holder.itemView.setBackgroundResource(R.drawable.panel_warning);
        }

    }

    @Override
    public int getItemCount() {
        return answerStates.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAnswer, tvCorrect, tvCorrectAnswerLabel, tvCorrectAnswer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAnswer = itemView.findViewById(R.id.tvAnswer);
            tvCorrect = itemView.findViewById(R.id.tvCorrect);
            tvCorrectAnswerLabel = itemView.findViewById(R.id.tvCorrectAnswerLabel);
            tvCorrectAnswer = itemView.findViewById(R.id.tvCorrectAnswer);
        }
    }
}
