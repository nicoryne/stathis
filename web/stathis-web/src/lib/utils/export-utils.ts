/**
 * Utilities for exporting data to different formats
 */

import { ScoreResponseDTO } from "@/services/progress/api-progress-client";

/**
 * Convert array data to CSV format
 */
export function arrayToCSV(data: any[], headers: string[]): string {
  // Start with headers
  const csvRows = [headers.join(',')];
  
  // Add data rows
  for (const row of data) {
    const values = headers.map(header => {
      const value = row[header] === null || row[header] === undefined ? '' : row[header];
      // Handle special characters and commas in CSV
      return typeof value === 'string' ? `"${value.replace(/"/g, '""')}"` : value;
    });
    csvRows.push(values.join(','));
  }
  
  return csvRows.join('\n');
}

/**
 * Create and download a CSV file from data
 */
export function downloadCSV(data: string, filename: string): void {
  const blob = new Blob([data], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  
  link.setAttribute('href', url);
  link.setAttribute('download', filename);
  link.style.display = 'none';
  
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

/**
 * Transform student score data into a format suitable for export
 */
export function transformScoresForExport(
  scores: ScoreResponseDTO[], 
  studentMap: Record<string, { firstName: string; lastName: string; email: string }>
): any[] {
  return scores.map(score => ({
    'Student ID': score.studentId,
    'Student Name': studentMap[score.studentId] 
      ? `${studentMap[score.studentId].firstName} ${studentMap[score.studentId].lastName}` 
      : 'Unknown',
    'Student Email': studentMap[score.studentId]?.email || 'N/A',
    'Task ID': score.taskId,
    'Task Name': score.taskName || score.taskId,
    'Task Type': score.taskType,
    'Score (%)': score.scoreValue,
    'Manual Score (%)': score.manualScore || 'N/A',
    'Completed': score.isCompleted ? 'Yes' : 'No',
    'Feedback': score.feedback || '',
    'Created Date': new Date(score.createdAt).toLocaleDateString(),
    'Last Updated': new Date(score.updatedAt).toLocaleDateString()
  }));
}

/**
 * Generate a student scores report as CSV
 */
export function exportStudentScoresReport(
  scores: ScoreResponseDTO[],
  students: { physicalId: string; firstName: string; lastName: string; email: string }[],
  classroomName: string = 'Classroom'
): void {
  // Create a lookup map for students
  const studentMap: Record<string, { firstName: string; lastName: string; email: string }> = {};
  students.forEach(student => {
    studentMap[student.physicalId] = {
      firstName: student.firstName,
      lastName: student.lastName,
      email: student.email
    };
  });
  
  // Transform scores data
  const exportData = transformScoresForExport(scores, studentMap);
  
  // Define headers
  const headers = [
    'Student ID', 
    'Student Name', 
    'Student Email', 
    'Task ID', 
    'Task Name', 
    'Task Type', 
    'Score (%)', 
    'Manual Score (%)', 
    'Completed',
    'Feedback',
    'Created Date',
    'Last Updated'
  ];
  
  // Convert to CSV
  const csvData = arrayToCSV(exportData, headers);
  
  // Generate filename with date
  const date = new Date().toISOString().split('T')[0];
  const filename = `${classroomName.replace(/\s+/g, '_')}_Student_Scores_${date}.csv`;
  
  // Download the file
  downloadCSV(csvData, filename);
}
