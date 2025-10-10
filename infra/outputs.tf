output "vpc_name" {
    value = google_compute.vpc_network.name
}

output "subnet_name" {
    value = google_compute_subnetwork.subnet.name
}

output "gke_cluster_name" {
    value = google_container_cluster.gke_cluster.name
}