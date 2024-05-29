package com.example.task101d.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.task101d.network.QuestionItem;
import com.example.task101d.R;

import java.text.MessageFormat;
import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.ViewHolder> {
    private final List<QuestionItem> questions;
    private final Context context;
    private final int[] selectedOptions;  // Stores the selected option for each question
    private final String[] selectedAnswers; // Stores  an array of all selected answers (as strings)

    public QuizAdapter(List<QuestionItem> questions, Context context) {
        this.questions = questions;
        this.context = context;
        this.selectedOptions = new int[questions.size()]; // Initialize the array
        this.selectedAnswers = new String[questions.size()]; // Initialize the array
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_question, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuestionItem question = questions.get(position);
        holder.tvTitle.setText(MessageFormat.format("Question {0}", position+1));
        holder.tvDescription.setText(question.getQuestion());
        holder.rbAnswer1.setText(question.getOptions()[0]);
        holder.rbAnswer2.setText(question.getOptions()[1]);
        holder.rbAnswer3.setText(question.getOptions()[2]);
        holder.rbAnswer4.setText(question.getOptions()[3]);

        // Set the position as a tag to identify the RadioButton
        holder.rbAnswer1.setTag(position);
        holder.rbAnswer2.setTag(position);
        holder.rbAnswer3.setTag(position);
        holder.rbAnswer4.setTag(position);

        // Set on click listener for RadioButtons
        holder.rbAnswer1.setOnClickListener(this::onRadioButtonClicked);
        holder.rbAnswer2.setOnClickListener(this::onRadioButtonClicked);
        holder.rbAnswer3.setOnClickListener(this::onRadioButtonClicked);
        holder.rbAnswer4.setOnClickListener(this::onRadioButtonClicked);
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    // Method to handle RadioButton clicks
    private void onRadioButtonClicked(View view) {
        RadioButton radioButton = (RadioButton) view;
        int position = (int) radioButton.getTag(); // Get the position of the RadioButton
        selectedOptions[position] = radioButton.getId(); // Store the selected option index
        selectedAnswers[position] = (String) radioButton.getText(); // And the selected option's text
    }

    // Getters for arrays of question data (user selected indexes and strings, and correct ones)
    public int[] getSelectedOptions() { return selectedOptions; }
    public String[] getSelectedAnswers() { return selectedAnswers; }
    public int[] getCorrectOptions() {
        int size = questions.size();
        int[] result = new int[size];
        for (int i = 0; i < size; i++) { result[i] = questions.get(i).getCorrectAnswerInt(); }
        return result;
    }
    public String[] getCorrectAnswers() {
        int size = questions.size();
        String[] result = new String[size];
        for (int i = 0; i < size; i++) { result[i] = questions.get(i).getOptions()[
                questions.get(i).getCorrectAnswerInt()]; }
        return result;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription;
        RadioButton rbAnswer1, rbAnswer2, rbAnswer3, rbAnswer4;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            rbAnswer1 = itemView.findViewById(R.id.rbAnswer1);
            rbAnswer2 = itemView.findViewById(R.id.rbAnswer2);
            rbAnswer3 = itemView.findViewById(R.id.rbAnswer3);
            rbAnswer4 = itemView.findViewById(R.id.rbAnswer4);
        }
    }
}
