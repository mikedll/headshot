
interface Window {
  tours: Tour[];
}

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

interface NestedMikeAttrs {
  name: string;
  age: number; 
}

interface Tour {
  id: number;
  name: string;
  createdAt: string;
}
