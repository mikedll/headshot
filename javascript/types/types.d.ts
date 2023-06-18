
type FileType = "file" | "dir";

interface DirFile {
  type: FileType;
  name: string;
}