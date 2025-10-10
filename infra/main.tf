resource "goole_compute_network" "vpc_network" {
    name = "sahil-vpc"
    auto_create_subnetworks = false
}

resource "google_compute_subnetwork" "subnet" {
    name = "sahil-subnet"
    ip_cidr_range = "10.0.0.0/24"
    region = var.region
    network = google_compute_network.vpc_network.id
}

resource "google_compute_firewall" "firewall" {
    name = "sahil-firewall"
    network = google_compute_network.vpc_network.name

    allow {
        protocol = "tcp"
        ports = ["22", "80", "3000", "5000", "9090", "9092"]
    }

    source_ranges = ["0.0.0.0/0"]
}