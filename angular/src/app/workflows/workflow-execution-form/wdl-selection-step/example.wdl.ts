export const md5sum = `
version 1.0

workflow md5Sum {
input {
File inputFile
}

call calculateMd5Sum {
input:
inputFile = inputFile
}

output {
String md5 = calculateMd5Sum.md5
}
}

task calculateMd5Sum {
input {
File inputFile
String outname = basename(inputFile) + ".md5"
}

Int diskSize = ceil(size(inputFile,"GB")) + 15

command <<<
gsutil hash -m ~{inputFile} | grep -E 'Hash \(md5\):\s' | cut -f4 > ~{outname}
>>>

output {
String md5 = read_string("\${outname}")
}

runtime {
docker: "google/cloud-sdk:slim"
cpu: 1
memory: "3.75 GB"
disks: "local-disk " + diskSize + " HDD"
}
}
`;

export const callDenovo = `
version 1.0

workflow CallDenovo {
\tinput {
\t\tFile family_vcf
\t\tFile family_vcf_index

\t\tString family_id
\t\tString proband_id
\t\tString father_id
\t\tString mother_id
\t\tString proband_sex

\t\tFile reference = "gs://genomics-public-data/resources/broad/hg38/v0/Homo_sapiens_assembly38.fasta"
\t\tFile reference_index = "gs://genomics-public-data/resources/broad/hg38/v0/Homo_sapiens_assembly38.fasta.fai"
\t\tFile reference_dict = "gs://genomics-public-data/resources/broad/hg38/v0/Homo_sapiens_assembly38.dict"

\t\tFile onekg_vcf = "gs://plenary-demo-private-datasets/reference/1000G_phase1.snps.high_confidence.hg38.BIALLELIC_ONLY.vcf.gz"
\t\tFile onekg_vcf_index = "gs://plenary-demo-private-datasets/reference/1000G_phase1.snps.high_confidence.hg38.BIALLELIC_ONLY.vcf.gz.tbi"
\t}

\tcall CreatePed {
\t\tinput:
\t\t\tfamily_id = family_id,
\t\t\tproband_id = proband_id,
\t\t\tmother_id = mother_id,
\t\t\tfather_id = father_id,
\t\t\tproband_sex = proband_sex
\t}

\tcall CalculateGenotypePosteriors {
\t\tinput:
\t\t\tfamily_vcf = family_vcf,
\t\t\tfamily_vcf_index = family_vcf_index,
\t\t\tfamily_ped = CreatePed.family_ped,
\t\t\tfamily_id = family_id,
\t\t\treference = reference,
\t\t\treference_index = reference_index,
\t\t\treference_dict = reference_dict,
\t\t\tonekg_vcf = onekg_vcf,
\t\t\tonekg_vcf_index = onekg_vcf_index
\t}

\tcall VariantFiltration {
\t\tinput:
\t\t\tposterior_vcf = CalculateGenotypePosteriors.posterior_vcf,
\t\t\tposterior_vcf_index = CalculateGenotypePosteriors.posterior_vcf_index,
\t\t\tfamily_id = family_id,
\t\t\treference = reference,
\t\t\treference_index = reference_index,
\t\t\treference_dict = reference_dict
\t}

\tcall VariantAnnotator {
\t\tinput:
\t\t\tfiltered_vcf = VariantFiltration.filtered_vcf,
\t\t\tfiltered_vcf_index = VariantFiltration.filtered_vcf_index,
\t\t\tfamily_ped = CreatePed.family_ped,
\t\t\tfamily_id = family_id,
\t\t\treference = reference,
\t\t\treference_index = reference_index,
\t\t\treference_dict = reference_dict
\t}

\tcall snpEff {
\t\tinput:
\t\t\tdenovo_vcf = VariantAnnotator.denovo_vcf,
\t\t\tdenovo_vcf_index = VariantAnnotator.denovo_vcf_index,
\t\t\tfamily_id = family_id
\t}

\tcall snpSift {
\t\tinput:
\t\t\tannotated_vcf = snpEff.annotated_vcf,
\t\t\tfamily_id = family_id
\t}

\toutput {
\t\tFile high_confidence_impact_denovos = snpSift.high_impact_vcf
\t\tFile snpEff_summary = snpEff.snpEff_summary
\t}
}

task CreatePed {
\tinput {
\t\tString family_id
\t\tString proband_id
\t\tString mother_id
\t\tString father_id
\t\tString proband_sex
\t}

\tcommand <<<
\t\tif [ "~{proband_sex}" = "F" ]; then
\t\t\tPROBAND_SEX=2
\t\telif [ "~{proband_sex}" = "M" ]; then
\t\t\tPROBAND_SEX=1
\t\telse
\t\t\tPROBAND_SEX=other
\t\tfi

\t\techo -e "~{family_id}\\t~{proband_id}\\t~{father_id}\\t~{mother_id}\\t$PROBAND_SEX\\t2" > ~{family_id}.ped
\t\techo -e "~{family_id}\\t~{father_id}\\t0\\t0\\t1\\t1" >> ~{family_id}.ped
\t\techo -e "~{family_id}\\t~{mother_id}\\t0\\t0\\t2\\t1" >> ~{family_id}.ped
\t>>>

\toutput {
\t\tFile family_ped = "~{family_id}.ped"
\t}

\truntime {
\t\tdocker: "ubuntu:xenial"
\t\tcpu: 1
\t\tmemory: "3.75 GB"
\t\tdisks: "local-disk 10 HDD"
\t}
}

task CalculateGenotypePosteriors {
\tinput {
\t\tFile family_vcf
\t\tFile family_vcf_index
\t\tFile family_ped
\t\tString family_id

\t\tFile reference
\t\tFile reference_index
\t\tFile reference_dict

\t\tFile onekg_vcf
\t\tFile onekg_vcf_index
\t}

\tInt disk_size = ceil((size(family_vcf, "GB") + size(reference, "GB") + size(onekg_vcf, "GB")) * 2 + 50)

\tcommand <<<
\t\tjava -jar /opt/extras/gatk/GenomeAnalysisTK.jar \\
\t\t\t-T CalculateGenotypePosteriors \\
\t\t\t-R ~{reference} \\
\t\t\t-supporting ~{onekg_vcf} \\
\t\t\t-V ~{family_vcf} \\
\t\t\t-pedValidationType SILENT \\
\t\t\t-ped ~{family_ped} \\
\t\t\t-o ~{family_id}.withPosteriors.vcf
\t>>>

\toutput {
\t\tFile posterior_vcf = "~{family_id}.withPosteriors.vcf"
\t\tFile posterior_vcf_index = "~{family_id}.withPosteriors.vcf.idx"
\t}

\truntime {
\t\tdocker: "gcr.io/cool-benefit-817/gatk:3.8.0"
\t\tcpu: 2
\t\tmemory: "7.5 GB"
\t\tdisks: "local-disk " + disk_size + " LOCAL"
\t}
}

task VariantFiltration {
\tinput {
\t\tFile posterior_vcf
\t\tFile posterior_vcf_index
\t\tString family_id

\t\tFile reference
\t\tFile reference_index
\t\tFile reference_dict
\t}

\tInt disk_size = ceil((size(posterior_vcf, "GB") + size(reference, "GB")) * 2 + 20)

\tcommand <<<
\t\tjava -jar /opt/extras/gatk/GenomeAnalysisTK.jar \\
\t\t\t-T VariantFiltration \\
\t\t\t-R ~{reference} \\
\t\t\t--variant ~{posterior_vcf} \\
\t\t\t--genotypeFilterExpression "GQ < 20" \\
\t\t\t--genotypeFilterName "GQ20" \\
\t\t\t-o ~{family_id}.filtered.vcf
\t>>>

\toutput {
\t\tFile filtered_vcf = "~{family_id}.filtered.vcf"
\t\tFile filtered_vcf_index = "~{family_id}.filtered.vcf.idx"
\t}

\truntime {
\t\tdocker: "gcr.io/cool-benefit-817/gatk:3.8.0"
\t\tcpu: 2
\t\tmemory: "7.5 GB"
\t\tdisks: "local-disk " + disk_size + " LOCAL"
\t}
}

task VariantAnnotator {
\tinput {
\t\tFile filtered_vcf
\t\tFile filtered_vcf_index
\t\tFile family_ped
\t\tString family_id

\t\tFile reference
\t\tFile reference_index
\t\tFile reference_dict
\t}

\tInt disk_size = ceil((size(filtered_vcf, "GB") + size(reference, "GB")) * 2 + 20)

\tcommand <<<
\t\tjava -jar /opt/extras/gatk/GenomeAnalysisTK.jar \\
\t\t-T VariantAnnotator \\
\t\t-R ~{reference} \\
\t\t-V ~{filtered_vcf} \\
\t\t-pedValidationType SILENT \\
\t\t-ped ~{family_ped} \\
\t\t-A PossibleDeNovo \\
\t\t-o ~{family_id}.denovo.vcf
\t>>>

\toutput {
\t\tFile denovo_vcf = "~{family_id}.denovo.vcf"
\t\tFile denovo_vcf_index = "~{family_id}.denovo.vcf.idx"
\t}

\truntime {
\t\tdocker: "gcr.io/cool-benefit-817/gatk:3.8.0"
\t\tcpu: 2
\t\tmemory: "7.5 GB"
\t\tdisks: "local-disk " + disk_size + " LOCAL"
\t}
}

task snpEff {
\tinput {
\t\tFile denovo_vcf
\t\tFile denovo_vcf_index
\t\tString family_id
\t}

\tInt disk_size = ceil(size(denovo_vcf, "GB") * 4 + 50)

\tcommand <<<
\t\t. snpEff_wrapper.sh \\
\t\t-C $CONFIG \\
\t\t-v ~{denovo_vcf} \\
\t\t-d GRCh38.86 \\
\t\t-S Homo_sapiens \\
\t\t-m Vertebrate_Mitochondrial

\t\t# first filter only denovos
\t\tjava -jar /opt/snpEff/SnpSift.jar filter \\
\t\t"(exists hiConfDeNovo)" ~{denovo_vcf} \\
\t\t> ~{family_id}.denovo_only.vcf

\t\t# annotate only high confidence de novo variants
\t\tjava -Xmx9G -jar /opt/snpEff/snpEff.jar \\
\t\t-c $CONFIG \\
\t\t$GENOME \\
\t\t~{family_id}.denovo_only.vcf \\
\t\t$ANNO_STRING \\
\t\t> ~{family_id}.denovo.annotated.vcf
\t>>>

\toutput {
\t\tFile annotated_vcf = "~{family_id}.denovo.annotated.vcf"
\t\tFile snpEff_summary = "snpEff_summary.html"
\t}

\truntime {
\t\tdocker: "gcr.io/cool-benefit-817/snpeff:4.3t"
\t\tcpu: 2
\t\tmemory: "13 GB"
\t\tdisks: "local-disk " + disk_size + " HDD"
\t}\t
}

task snpSift {
\tinput {
\t\tFile annotated_vcf
\t\tString family_id
\t}

\tInt disk_size = ceil(size(annotated_vcf, "GB") * 2 + 20)

\tcommand <<<
\t\tjava -jar /opt/snpEff/SnpSift.jar filter \\
\t\t"(ANN[*].IMPACT = 'HIGH')" \\
\t\t~{annotated_vcf} \\
\t\t> ~{family_id}.denovo.annotated.high_impact.vcf
\t>>>

\toutput {
\t\tFile high_impact_vcf = "~{family_id}.denovo.annotated.high_impact.vcf"
\t}

\truntime {
\t\tdocker: "gcr.io/cool-benefit-817/snpeff:4.3t"
\t\tcpu: 2
\t\tmemory: "13 GB"
\t\tdisks: "local-disk " + disk_size + " HDD"
\t}\t
}
`;

export const helloWorld = `version 1.0

workflow hello {
\tinput {
\t\tString name
\t}

\tcall sayHello {
\t\tinput:
\t\t\tname = name
\t}

\toutput {
\t\tString greeting = sayHello.greeting
\t}
}

task sayHello {
\tinput {
\t\tString name
\t}

\tcommand {
\t\techo "Hello ~{name}"
\t}

\toutput {
\t\tString greeting = read_string(stdout())
\t}

\truntime {
\t\tdocker: "ubuntu:xenial"
\t}
}
`;
