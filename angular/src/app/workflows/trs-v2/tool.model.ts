import ToolClass from './tool-class.model';
import ToolVersion from './tool-version.model';


export default interface Tool {
  url: string;
  id: string;
  aliases: string[];
  organization: string;
  toolname: string;
  toolclass: ToolClass;
  description: string;
  author: string;
  meta_version: string;
  contains: string[];
  has_checker: boolean;
  checker_url: string;
  verified: boolean;
  verified_source: string;
  signed: boolean;
  versions: ToolVersion[];
}
