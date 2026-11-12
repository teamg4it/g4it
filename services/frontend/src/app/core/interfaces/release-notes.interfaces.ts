export interface ReleaseNotesPublishRest {
    content: string
}

export type ReleaseNoteRest = {
    id: number
    content: string
} | null
