
type FileType = "file" | "dir";

interface GithubFile {
  type: FileType;
  name: string;
  content: string;
  isText: boolean;
}

interface GithubPath {
  path: string;
  isFile: boolean;
  files: GithubFile[];
}