export function getImage(label: string): string {
    if (label == "Employee") {
        return './images/person.svg';
    } else if (label == "CalendarEvent") {
        return './images/calendar.svg';
    } else if (label == "Project") {
        return './images/project.svg';
    } else if (label == "CodeReview") {
        return './images/review.svg';
    }
    return ''
}