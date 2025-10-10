variable "project_id" {
    description = "GCP Project ID"
    type = string
}

variable "region" {
    description = "GCP Region"
    type = string
    default = "us-central1"
}

variable "zone" {
    description = "GCP Zone"
    type = string
    default = "us-central1-a"
}

variable "gke_node_count" {
    description = "Number of nodes in the GKE cluster"
    type = number
    default = 1
}

variable "gke_machine_type" {
    description = "Machine type for gke nodes"
    type = string
    default = "e2-medium"
}

