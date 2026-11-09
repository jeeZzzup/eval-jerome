package com.eex.domain.service;

import com.eex.domain.entity.Client;
import com.eex.infrastructure.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Optional<Client> getClientById(Integer id) {
        return clientRepository.findById(id);
    }

    public Client createClient(Client client) {
        return clientRepository.save(client);
    }

    public Client updateClient(Integer id, Client clientDetails) {
        Optional<Client> optionalClient = clientRepository.findById(id);
        if (optionalClient.isPresent()) {
            Client client = optionalClient.get();
            if (clientDetails.getLegalEntity() != null) {
                client.setLegalEntity(clientDetails.getLegalEntity());
            }
            return clientRepository.save(client);
        }
        throw new RuntimeException("Client introuvable avec l'id " + id);
    }

    public void deleteClient(Integer id) {
        if (clientRepository.existsById(id)) {
            clientRepository.deleteById(id);
        } else {
            throw new RuntimeException("Client introuvable avec l'id " + id);
        }
    }
}

