export default interface ToolVersion {
  name: string;
  url: string;
  id: string;
  image: string;
  registry_url: string;
  image_name: string;
  descriptor_type: string[];
  containerfile: boolean;
  meta_version: string;
  verified: boolean;
  verified_source: string;
}
